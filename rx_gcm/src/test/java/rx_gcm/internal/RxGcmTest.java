/*
 * Copyright 2016 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx_gcm.internal;

import android.app.Application;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.observers.TestSubscriber;
import rx_gcm.BackgroundMessage;
import rx_gcm.ForegroundMessage;
import rx_gcm.GcmForegroundReceiver;
import rx_gcm.TokenUpdate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RxGcmTest {
    @Mock protected Application applicationMock;
    @Mock protected GetGcmServerToken getGcmServerTokenMock;
    @Mock protected Persistence persistenceMock;
    @Mock protected ActivitiesLifecycleCallbacks activitiesLifecycle;
    @Mock protected GetGcmForegroundReceivers getGcmForegroundReceivers;

    private final static String MOCK_TOKEN = "mock_token";

    @Before public void setUp()  {
        MockitoAnnotations.initMocks(this);
        RxGcm.Notifications.initForTesting(getGcmServerTokenMock, persistenceMock, activitiesLifecycle, getGcmForegroundReceivers);
        when(activitiesLifecycle.getApplication()).thenReturn(applicationMock);
    }

    @Test public void When_No_Saved_Token_Call_GcmServer_And_Save_It() throws Exception {
        when(getGcmServerTokenMock.retrieve(applicationMock)).thenReturn(MOCK_TOKEN);
        when(persistenceMock.getToken(applicationMock)).thenReturn(null);

        TestSubscriber<String> subscriberMock = new TestSubscriber<>();
        RxGcm.Notifications.register(applicationMock).subscribe(subscriberMock);
        subscriberMock.awaitTerminalEvent();

        subscriberMock.assertValue(MOCK_TOKEN);
        verify(persistenceMock, times(1)).saveToken(MOCK_TOKEN, applicationMock);
    }

    @Test public void When_No_Saved_Token_And_Call_GcmServer_Error_Emit_Error_But_Try_3_Times() throws Exception {
        String errorMessage = "GCM not available";

        when(getGcmServerTokenMock.retrieve(applicationMock)).thenThrow(new RuntimeException(errorMessage));
        when(persistenceMock.getToken(applicationMock)).thenReturn(null);

        TestSubscriber<String> subscriberMock = new TestSubscriber<>();
        RxGcm.Notifications.register(applicationMock).subscribe(subscriberMock);
        subscriberMock.awaitTerminalEvent();

        subscriberMock.assertError(RuntimeException.class);
        verify(persistenceMock, times(0)).saveToken(MOCK_TOKEN, applicationMock);
        verify(getGcmServerTokenMock, times(3)).retrieve(applicationMock);
    }

    @Test public void When_Saved_Token_No_Call_GcmServer_And_Not_Emit_Items() throws Exception {
        when(getGcmServerTokenMock.retrieve(applicationMock)).thenReturn(MOCK_TOKEN);
        when(persistenceMock.getToken(applicationMock)).thenReturn(MOCK_TOKEN);

        TestSubscriber<String> subscriberMock = new TestSubscriber<>();
        RxGcm.Notifications.register(applicationMock).subscribe(subscriberMock);
        subscriberMock.awaitTerminalEvent();

        subscriberMock.assertNoValues();
        subscriberMock.assertNoErrors();
        subscriberMock.assertCompleted();

        verify(getGcmServerTokenMock, times(0)).retrieve(applicationMock);
    }

    @Test public void When_Call_Current_Token_And_There_Is_A_Token_Emit_It() {
        when(persistenceMock.getToken(applicationMock)).thenReturn(MOCK_TOKEN);

        TestSubscriber<String> subscriberMock = new TestSubscriber<>();
        RxGcm.Notifications.currentToken().subscribe(subscriberMock);
        subscriberMock.awaitTerminalEvent();

        subscriberMock.assertValue(MOCK_TOKEN);
        subscriberMock.assertNoErrors();
    }

    @Test public void When_Call_Current_Token_And_There_Is_No_Token_Emit_Error_But_Try_3_Times() {
        when(persistenceMock.getToken(applicationMock)).thenReturn(null);

        TestSubscriber<String> subscriberMock = new TestSubscriber<>();
        RxGcm.Notifications.currentToken().subscribe(subscriberMock);
        subscriberMock.awaitTerminalEvent();

        subscriberMock.assertError(RuntimeException.class);
        subscriberMock.assertValueCount(0);
        verify(persistenceMock, times(3)).getToken(applicationMock);
    }

    @Test public void When_Call_On_Token_Refresh_Emit_Properly_Item() throws Exception {
        TestSubscriber<TokenUpdate> subscriberMock = GcmRefreshTokenReceiverMock.initSubscriber();
        when(persistenceMock.getClassNameGcmRefreshTokenReceiver(applicationMock)).thenReturn(GcmRefreshTokenReceiverMock.class.getName());

        when(getGcmServerTokenMock.retrieve(applicationMock)).thenReturn(MOCK_TOKEN);
        RxGcm.Notifications.onTokenRefreshed();
        subscriberMock.awaitTerminalEvent();
        subscriberMock.assertNoErrors();
        TokenUpdate token1 = subscriberMock.getOnNextEvents().get(0);
        assertThat(token1.getToken(), is(MOCK_TOKEN));

        reset(getGcmServerTokenMock);
        String errorMessage = "GCM not available";
        when(getGcmServerTokenMock.retrieve(applicationMock)).thenThrow(new RuntimeException(errorMessage));        RxGcm.Notifications.onTokenRefreshed();
        subscriberMock.awaitTerminalEvent();
        subscriberMock.assertValueCount(1);
        assertThat(subscriberMock.getOnErrorEvents().get(0).getMessage(), is(errorMessage));

        reset(getGcmServerTokenMock);
        when(getGcmServerTokenMock.retrieve(applicationMock)).thenReturn(MOCK_TOKEN + 1);
        RxGcm.Notifications.onTokenRefreshed();
        subscriberMock.awaitTerminalEvent();
        assertThat(subscriberMock.getOnErrorEvents().size(), is(1));
        TokenUpdate token2 = subscriberMock.getOnNextEvents().get(1);
        assertThat(token2.getToken(), is(MOCK_TOKEN + 1));

        reset(getGcmServerTokenMock);
        when(persistenceMock.getClassNameGcmRefreshTokenReceiver(applicationMock)).thenReturn(null);
        try {
            RxGcm.Notifications.onTokenRefreshed();
            subscriberMock.awaitTerminalEvent();
        } catch (Exception ignore) {
            assertThat(subscriberMock.getOnErrorEvents().size(), is(1));
            subscriberMock.assertValueCount(2);
        }
    }


    @Test public void When_Call_On_Background_Notification_Emit_Properly_Item() {
        when(activitiesLifecycle.isAppOnBackground()).thenReturn(true);
        Bundle payload = new Bundle();

        GcmBackgroundReceiverMock.initSubscriber();

        when(persistenceMock.getClassNameGcmBackgroundReceiver(applicationMock)).thenReturn(GcmBackgroundReceiverMock.class.getName());
        String from1 = "MockServer1";
        RxGcm.Notifications.onNotificationReceived(from1, payload);
        String from2 = "MockServer2";
        RxGcm.Notifications.onNotificationReceived(from2, payload);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        List<BackgroundMessage> backgroundMessages = GcmBackgroundReceiverMock.getBackgroundMessages();
        assertThat(backgroundMessages.get(0).getFrom(), is(from1));
        assertThat(backgroundMessages.get(1).getFrom(), is(from2));
        assertThat(backgroundMessages.size(), is(2));

        when(persistenceMock.getClassNameGcmBackgroundReceiver(applicationMock)).thenReturn(null);

        try {
            RxGcm.Notifications.onNotificationReceived("", payload);
            assert false;
        } catch (Exception ignore) {}
    }

    @Test public void When_Call_On_Foreground_Notification_Emit_Properly_Item_Centralized_Version() {
        when(activitiesLifecycle.isAppOnBackground()).thenReturn(false);
        when(getGcmForegroundReceivers.retrieve(null)).thenReturn((List<GcmForegroundReceiver>) (List<?>) getForegroundReceivers());

        Bundle payload = new Bundle();

        TestSubscriber<ForegroundMessage> subscriberMock = new TestSubscriber();
        RxGcm.Notifications.onForegroundNotification().subscribe(subscriberMock);

        String from1 = "MockServer1";
        RxGcm.Notifications.onNotificationReceived(from1, payload);
        subscriberMock.assertValueCount(1);
        assertThat(subscriberMock.getOnNextEvents().get(0).getFrom(), is(from1));
        subscriberMock.assertNoErrors();

        String from2 = "MockServer2";
        RxGcm.Notifications.onNotificationReceived(from2, payload);
        subscriberMock.assertValueCount(2);
        assertThat(subscriberMock.getOnNextEvents().get(1).getFrom(), is(from2));
        subscriberMock.assertValueCount(2);
        subscriberMock.assertNoErrors();
    }

    @Test public void When_Call_On_Foreground_Notification_Emit_Properly_Item_Decentralized_Version() {
        List<GcmForegroundReceiverMock> foregroundReceivers = getForegroundReceivers();
        when(activitiesLifecycle.isAppOnBackground()).thenReturn(false);
        when(getGcmForegroundReceivers.retrieve(null)).thenReturn((List<GcmForegroundReceiver>) (List<?>) foregroundReceivers);

        Bundle payload = new Bundle();

        String from1 = "MockServer1";
        RxGcm.Notifications.onNotificationReceived(from1, payload);
        String from2 = "MockServer2";
        RxGcm.Notifications.onNotificationReceived(from2, payload);
        String from3 = "MockServer3";
        RxGcm.Notifications.onNotificationReceived(from3, payload);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        for (GcmForegroundReceiverMock receiver : foregroundReceivers) {
            List<ForegroundMessage> messages = receiver.getForegroundMessages();
            assertThat(messages.get(0).getFrom(), is(from1));
            assertThat(messages.get(1).getFrom(), is(from2));
            assertThat(messages.get(2).getFrom(), is(from3));
        }
    }

    private List<GcmForegroundReceiverMock> getForegroundReceivers() {
        List<GcmForegroundReceiverMock> foregroundReceivers = new ArrayList();
        foregroundReceivers.add(new GcmForegroundReceiverMock());
        foregroundReceivers.add(new GcmForegroundReceiverMock());
        foregroundReceivers.add(new GcmForegroundReceiverMock());
        return foregroundReceivers;
    }

    @Test(expected=IllegalStateException.class) public void When_Call_Class_With_No_Public_Empty_Constructor_Get_Exception() {
        RxGcm.Notifications.getInstanceClassByName(ClassWithNoPublicEmptyConstructor.class.getName());
    }

    @Test public void When_Call_Class_With_Public_Empty_Constructor_Not_Get_Exception() {
        RxGcm.Notifications.getInstanceClassByName(ClassWithPublicEmptyConstructor.class.getName());
    }
}
