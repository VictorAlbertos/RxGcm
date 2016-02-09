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

import android.app.Application;
import android.os.Bundle;

/**
 * Entity received as the message when application is on background state.
 */
public class BackgroundMessage {
    private final Application application;
    private final String from;
    private final Bundle payload;

    public BackgroundMessage(Application application, String from, Bundle bundle) {
        this.application = application;
        this.from = from;
        this.payload = bundle;
    }

    public Application getApplication() {
        return application;
    }

    public Bundle getPayload() {
        return payload;
    }

    public String getFrom() {
        return from;
    }
}
