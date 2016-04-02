package victoralbertos.io.rxgcm.presentation;

import android.app.Application;
import android.widget.Toast;

import rx_gcm.internal.RxGcm;
import victoralbertos.io.rxgcm.AppGcmReceiverData;
import victoralbertos.io.rxgcm.AppGcmReceiverUIBackground;
import victoralbertos.io.rxgcm.RefreshTokenReceiver;


public class RxSampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        RxGcm.Notifications
                .register(this, AppGcmReceiverData.class, AppGcmReceiverUIBackground.class)
                .subscribe(
                        token -> showMessage("Device register with token: " + token),
                        error -> showMessage("Error when trying to register: " + error.getMessage()
                        ));

        RxGcm.Notifications.onRefreshToken(RefreshTokenReceiver.class);
    }

    private void showMessage(String message) {
        Toast.makeText(RxSampleApp.this, message, Toast.LENGTH_LONG).show();
    }
}
