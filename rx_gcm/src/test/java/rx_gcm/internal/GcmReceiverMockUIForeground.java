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

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx_gcm.ForegroundMessage;
import rx_gcm.GcmReceiverUIForeground;

public class GcmReceiverMockUIForeground implements GcmReceiverUIForeground {
    private final List<ForegroundMessage> messages;
    private long onNotificationStartTimeStamp;

    public GcmReceiverMockUIForeground() {
        this.messages = new ArrayList<>();
    }

    @Override public void onNotification(Observable<ForegroundMessage> oForegroundMessage) {
        onNotificationStartTimeStamp = System.currentTimeMillis();

        oForegroundMessage.subscribe(new Action1<ForegroundMessage>() {
            @Override public void call(ForegroundMessage foregroundMessage) {
                messages.add(foregroundMessage);
            }
        });
    }

    @Override public String target() {
        return "GcmReceiverMockUI";
    }

    public List<ForegroundMessage> getMessages() {
        return messages;
    }

    public long getOnNotificationStartTimeStamp() {
        return onNotificationStartTimeStamp;
    }
}
