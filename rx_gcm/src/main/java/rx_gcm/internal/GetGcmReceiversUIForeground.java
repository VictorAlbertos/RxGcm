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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import rx_gcm.GcmReceiverUIForeground;

class GetGcmReceiversUIForeground {
    private List<Fragment> gcmReceiversUIForegroundNotTargetScreen;

    Wrapper retrieve(String screenName, Activity activity){
        Wrapper receiverCandidate = null;

        if (activity == null) return receiverCandidate;

        if (activity instanceof GcmReceiverUIForeground) {
            GcmReceiverUIForeground gcmReceiverUIForeground = (GcmReceiverUIForeground) activity;

            boolean targetScreen = gcmReceiverUIForeground.matchesTarget(screenName);
            receiverCandidate = new Wrapper(gcmReceiverUIForeground, targetScreen);

            if (targetScreen) return receiverCandidate;
        }

        if (!(activity instanceof FragmentActivity)) return receiverCandidate;

        FragmentActivity fragmentActivity = (FragmentActivity) activity;
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

        gcmReceiversUIForegroundNotTargetScreen = new ArrayList<>();
        Fragment fragment = getGcmReceiverUIForeground(fragmentManager.getFragments(), screenName);
        gcmReceiversUIForegroundNotTargetScreen.clear();

        if (fragment != null) {
            GcmReceiverUIForeground gcmReceiverUIForeground = (GcmReceiverUIForeground) fragment;
            boolean isTargetScreen = gcmReceiverUIForeground.matchesTarget(screenName);
            return new Wrapper(gcmReceiverUIForeground, isTargetScreen);
        } else {
            return receiverCandidate;
        }
    }

    @Nullable
    private Fragment getGcmReceiverUIForeground(List<Fragment> fragments, String screenName) {
        if (fragments == null) return null;

        for (Fragment fragment : fragments) {
            if (fragment != null && isVisible(fragment) && fragment instanceof GcmReceiverUIForeground) {
                GcmReceiverUIForeground gcmReceiverUIForeground = (GcmReceiverUIForeground) fragment;
                boolean isTargetScreen = gcmReceiverUIForeground.matchesTarget(screenName);

                if (isTargetScreen) return fragment;

                gcmReceiversUIForegroundNotTargetScreen.add(fragment);

                if (fragment.getChildFragmentManager() != null) {
                    Fragment candidate = getGcmReceiverUIForegroundFromChild(fragment, screenName);
                    if (candidate != null) return candidate;
                }
            } else if (fragment != null && isVisible(fragment) && fragment.getChildFragmentManager() != null) {
                Fragment candidate = getGcmReceiverUIForegroundFromChild(fragment, screenName);
                if (candidate != null) return candidate;
            }
        }

        if (!gcmReceiversUIForegroundNotTargetScreen.isEmpty()) return gcmReceiversUIForegroundNotTargetScreen.get(0);
        else return null;
    }

    private Fragment getGcmReceiverUIForegroundFromChild(Fragment fragment, String screenName) {
        List<Fragment> childFragments = fragment.getChildFragmentManager().getFragments();
        Fragment candidate = getGcmReceiverUIForeground(childFragments, screenName);

        if (candidate == null) return null;

        GcmReceiverUIForeground gcmReceiverUIForeground = (GcmReceiverUIForeground) candidate;
        boolean isTargetScreen = gcmReceiverUIForeground.matchesTarget(screenName);
        if (isTargetScreen) return candidate;

        gcmReceiversUIForegroundNotTargetScreen.add(candidate);

        return null;
    }

    static class Wrapper {
        private final GcmReceiverUIForeground gcmReceiverUIForeground;
        private final boolean targetScreen;

        public Wrapper(GcmReceiverUIForeground gcmReceiverUIForeground, boolean targetScreen) {
            this.gcmReceiverUIForeground = gcmReceiverUIForeground;
            this.targetScreen = targetScreen;
        }

        public GcmReceiverUIForeground gcmReceiverUIForeground() {
            return gcmReceiverUIForeground;
        }

        public boolean isTargetScreen() {
            return targetScreen;
        }
    }

    //exists for testing purposes
    private boolean mock;
    void mockForTestingPurposes() {
        mock = true;
    }

    //exists for testing purposes
    boolean isVisible(Fragment fragment) {
        if (mock) return true;
        return fragment.getUserVisibleHint();
    }
}
