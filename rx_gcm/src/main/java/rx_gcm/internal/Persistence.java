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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class Persistence {

    void saveClassNameGcmReceiverAndGcmReceiverUIBackground(String gcmReceiverClassName, String gcmReceiverUIBackgroundClassName, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPreferences.edit()
                .putString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER, gcmReceiverClassName)
                .putString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER_UI_BACKGROUND, gcmReceiverUIBackgroundClassName)
                .apply();
    }

    String getClassNameGcmReceiver(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER, null);
    }

    String getClassNameGcmReceiverUIBackground(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER_UI_BACKGROUND, null);
    }

    void saveClassNameGcmRefreshTokenReceiver(String name, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_REFRESH_TOKEN, name).apply();
    }

    String getClassNameGcmRefreshTokenReceiver(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_REFRESH_TOKEN, null);
    }

    void saveToken(String token, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(Constants.KEY_SHARED_PREFERENCES_TOKEN, token).apply();
    }

    String getToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.KEY_SHARED_PREFERENCES_TOKEN, null);
    }

}
