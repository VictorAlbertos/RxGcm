package victoralbertos.io.rxgcm.presentation;

import rx.Observable;
import rx_gcm.Message;
import victoralbertos.io.rxgcm.data.api.GcmServerService;

/**
 * Created by victor on 08/02/16.
 */
public class FragmentSupplies extends FragmentBase {

    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }

    @Override public boolean matchesTarget(String key) {
        return GcmServerService.TARGET_SUPPLY_GCM.equals(key);
    }
}
