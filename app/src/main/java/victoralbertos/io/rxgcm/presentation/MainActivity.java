package victoralbertos.io.rxgcm.presentation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import rx.Observable;
import rx_gcm.ForegroundMessage;
import rx_gcm.GcmForegroundReceiver;
import victoralbertos.io.rxgcm.BackgroundMessageReceiver;
import victoralbertos.io.rxgcm.R;
import victoralbertos.io.rxgcm.api.GcmServerService;

public class MainActivity extends AppCompatActivity implements GcmForegroundReceiver {
    private GcmServerService gcmServerService;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendMessageClick();
        gcmServerService = new GcmServerService();
    }

    private void sendMessageClick() {
        findViewById(R.id.bt_send_notification).setOnClickListener(view -> {
            String title = ((EditText) findViewById(R.id.et_title)).getText().toString();
            String body = ((EditText) findViewById(R.id.et_body)).getText().toString();

            gcmServerService.sendGcmNotification(title, body).subscribe(success -> {
                if (success) Log.d("sendGcmNotification", "Message sent");
                else Log.e("sendGcmNotification", "Message not sent");
            });
        });
    }

    @Override public void onReceiveMessage(Observable<ForegroundMessage> oMessage) {
        final TextView tvTitle = (TextView) findViewById(R.id.tv_title_activity);
        final TextView tvBody = (TextView) findViewById(R.id.tv_body_activity);

        oMessage.doOnError(error -> tvTitle.setText(error.getMessage()))
                .subscribe(message -> {
                    String title = message.getPayload().getString(BackgroundMessageReceiver.TITLE);
                    tvTitle.setText(title);
                    String body = message.getPayload().getString(BackgroundMessageReceiver.BODY);
                    tvBody.setText(body);
                });
    }

    public void receivedFromCentralized(ForegroundMessage message) {
        final TextView tvTitle = (TextView) findViewById(R.id.tv_title_centralized);
        final TextView tvBody = (TextView) findViewById(R.id.tv_body_centralized);

        String title = message.getPayload().getString(BackgroundMessageReceiver.TITLE);
        tvTitle.setText(title);
        String body = message.getPayload().getString(BackgroundMessageReceiver.BODY);
        tvBody.setText(body);
    }
}
