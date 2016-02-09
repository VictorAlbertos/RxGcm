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

package rx_gcm;

import android.app.Activity;
import android.os.Bundle;

/**
 * Entity received as the message when application is on foreground state.
 */
public class ForegroundMessage {
    private final Activity currentActivity;
    private final String from;
    private final Bundle payload;

    public ForegroundMessage(Activity currentActivity, String from, Bundle bundle) {
        this.currentActivity = currentActivity;
        this.from = from;
        this.payload = bundle;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public Bundle getPayload() {
        return payload;
    }

    public String getFrom() {
        return from;
    }
}
