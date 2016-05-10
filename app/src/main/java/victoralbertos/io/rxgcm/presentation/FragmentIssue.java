package victoralbertos.io.rxgcm.presentation;

import rx.Observable;
import rx_gcm.Message;
import victoralbertos.io.rxgcm.data.api.GcmServerService;

/**
 * Created by victor on 03/04/16.
 */
public class FragmentIssue extends FragmentBase {

    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }

    @Override public boolean matchesTarget(String key) {
        return GcmServerService.TARGET_ISSUE_GCM.equals(key);
    }
}
