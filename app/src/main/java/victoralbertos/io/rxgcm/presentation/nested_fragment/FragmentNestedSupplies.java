package victoralbertos.io.rxgcm.presentation.nested_fragment;

import android.os.Bundle;
import android.view.View;

import rx.Observable;
import rx_gcm.Message;
import victoralbertos.io.rxgcm.R;
import victoralbertos.io.rxgcm.data.api.GcmServerService;
import victoralbertos.io.rxgcm.presentation.FragmentBase;

/**
 * Created by victor on 08/02/16.
 */
public class FragmentNestedSupplies extends FragmentBase {

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sendNestedSupplyClickListener();

        findViewById(R.id.bt_go_to_other_screen).setVisibility(View.GONE);
        findViewById(R.id.bt_send_issue).setVisibility(View.GONE);
    }

    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }

    @Override public boolean matchesTarget(String key) {
        return GcmServerService.TARGET_NESTED_SUPPLY_GCM.equals(key);
    }

}
