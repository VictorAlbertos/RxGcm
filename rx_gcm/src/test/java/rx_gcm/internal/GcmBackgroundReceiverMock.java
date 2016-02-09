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
import rx_gcm.BackgroundMessage;
import rx_gcm.GcmBackgroundReceiver;

public class GcmBackgroundReceiverMock implements GcmBackgroundReceiver {
    private static List<BackgroundMessage> backgroundMessages;

    public static void initSubscriber() {
        backgroundMessages = new ArrayList<>();
    }

    @Override public void onMessage(Observable<BackgroundMessage> oMessage) {
        oMessage.subscribe(new Action1<BackgroundMessage>() {
            @Override public void call(BackgroundMessage backgroundMessage) {
                backgroundMessages.add(backgroundMessage);
            }
        });
    }

    public static List<BackgroundMessage> getBackgroundMessages() {
        return backgroundMessages;
    }

}
