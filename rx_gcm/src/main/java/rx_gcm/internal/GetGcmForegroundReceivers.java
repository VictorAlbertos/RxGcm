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


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import rx_gcm.GcmForegroundReceiver;

class GetGcmForegroundReceivers {

    List<GcmForegroundReceiver> retrieve(Activity activity){
        List<GcmForegroundReceiver> foregroundReceivers = new ArrayList<>();

        if (activity == null) return foregroundReceivers;

        if (activity instanceof GcmForegroundReceiver) foregroundReceivers.add((GcmForegroundReceiver) activity);

        if (!(activity instanceof FragmentActivity)) return foregroundReceivers;

        FragmentActivity fragmentActivity = (FragmentActivity) activity;
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

        List<Fragment> fragments = fragmentManager.getFragments();

        if(fragments != null) {
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible() && fragment instanceof GcmForegroundReceiver) {
                    GcmForegroundReceiver foregroundReceiver = (GcmForegroundReceiver) fragment;
                    foregroundReceivers.add(foregroundReceiver);
                }
            }
        }

        return foregroundReceivers;
    }
}
