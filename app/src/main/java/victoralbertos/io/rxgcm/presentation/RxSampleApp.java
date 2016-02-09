package victoralbertos.io.rxgcm.presentation;

import android.app.Application;
import android.widget.Toast;

import rx.Subscriber;
import rx_gcm.ForegroundMessage;
import rx_gcm.internal.RxGcm;
import victoralbertos.io.rxgcm.BackgroundMessageReceiver;
import victoralbertos.io.rxgcm.RefreshTokenReceiver;


public class RxSampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        RxGcm.Notifications.register(this)
                .subscribe(new Subscriber<String>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable error) {
                        Toast.makeText(RxSampleApp.this, "Error when trying to register: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override public void onNext(String token) {
                        Toast.makeText(RxSampleApp.this, "Device register with token: " + token, Toast.LENGTH_LONG).show();
                    }
                });

        RxGcm.Notifications.onBackgroundNotification(BackgroundMessageReceiver.class);
        RxGcm.Notifications.onRefreshToken(RefreshTokenReceiver.class);

        RxGcm.Notifications.onForegroundNotification().subscribe(new Subscriber<ForegroundMessage>() {
            @Override public void onCompleted() {}

            @Override public void onError(Throwable e) {
                String error = "Error from centralized receiver: " + e.getMessage();
                Toast.makeText(RxSampleApp.this, error, Toast.LENGTH_LONG).show();
            }

            @Override public void onNext(ForegroundMessage foregroundMessage) {
                ((MainActivity)foregroundMessage.getCurrentActivity()).receivedFromCentralized(foregroundMessage);
            }
        });
    }
}
