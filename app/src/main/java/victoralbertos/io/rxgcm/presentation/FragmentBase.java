package victoralbertos.io.rxgcm.presentation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx_gcm.GcmReceiverUIForeground;
import rx_gcm.Message;
import victoralbertos.io.rxgcm.R;
import victoralbertos.io.rxgcm.data.Cache;
import victoralbertos.io.rxgcm.data.api.GcmServerService;
import victoralbertos.io.rxgcm.data.entities.Notification;

/**
 * Created by victor on 08/02/16.
 */
public abstract class FragmentBase extends Fragment implements GcmReceiverUIForeground {
    private GcmServerService gcmServerService;
    protected NotificationAdapter notificationAdapter;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_fragment, container, false);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sendIssueClickListener();
        sendSupplyClickListener();
        goToSuppliesClickListener();
        setUpRecyclerView();

        gcmServerService = new GcmServerService();
    }

    private void sendIssueClickListener() {
        findViewById(R.id.bt_send_issue).setOnClickListener(view -> {
            String title = ((EditText) findViewById(R.id.et_title)).getText().toString();
            String body = ((EditText) findViewById(R.id.et_body)).getText().toString();

            gcmServerService.sendGcmNotificationRequestingIssue(title, body).subscribe(success -> {
                if (success) Log.d("sendGcmNotification", "Message sent");
                else Log.e("sendGcmNotification", "Message not sent");
            });
        });
    }

    private void sendSupplyClickListener() {
        findViewById(R.id.bt_send_supply).setOnClickListener(view -> {
            String title = ((EditText) findViewById(R.id.et_title)).getText().toString();
            String body = ((EditText) findViewById(R.id.et_body)).getText().toString();

            gcmServerService.sendGcmNotificationRequestingSupply(title, body).subscribe(success -> {
                if (success) Log.d("sendGcmNotification", "Message sent");
                else Log.e("sendGcmNotification", "Message not sent");
            });
        });
    }

    private void goToSuppliesClickListener() {
        Button button = (Button) findViewById(R.id.bt_go_to_other_screen);
        button.setText(this instanceof FragmentSupplies ? "Go to issues" : "Go to supplies");

        button.setOnClickListener(view -> {
            Class<? extends Activity> clazz = this instanceof FragmentSupplies ? HostActivityIssues.class : HostActivitySupplies.class;
            startActivity(new Intent(getActivity(), clazz));
        });
    }

    private void setUpRecyclerView() {
        RecyclerView rv_notifications = (RecyclerView) findViewById(R.id.rv_notifications);
        rv_notifications.setHasFixedSize(true);
        rv_notifications.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<Notification> notifications = this instanceof FragmentSupplies ? Cache.Pool.getSupplies() : Cache.Pool.getIssues();
        notificationAdapter = new NotificationAdapter(notifications);
        rv_notifications.setAdapter(notificationAdapter);
    }

    @Override public void onMismatchTargetNotification(Observable<Message> oMessage) {
        showAlert();
    }

    public static final String MISMATCH_TARGET_MESSAGE = "New notification has been added. Go and check it!";
    public void showAlert() {
        TextView tv_log = (TextView) findViewById(R.id.tv_log);

        Observable.just("")
                .doOnNext(empty -> tv_log.setText(MISMATCH_TARGET_MESSAGE))
                .delay(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(empty -> tv_log.setText(empty));
    }


    private View findViewById(int id) {
        return getView().findViewById(id);
    }
}
