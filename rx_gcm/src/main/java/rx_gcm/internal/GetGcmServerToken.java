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


import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import rx_gcm.internal.Constants;
import victoralbertos.io.rx_gcm.R;

class GetGcmServerToken {

    String retrieve(Context context) throws Exception {

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        if(status != ConnectionResult.SUCCESS) {
            throw new RuntimeException(Constants.GOOGLE_PLAY_SERVICES_ERROR);
        }

        InstanceID instanceID = InstanceID.getInstance(context);
        return instanceID.getToken(context.getString(R.string.gcm_defaultSenderId),
                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
    }
}
