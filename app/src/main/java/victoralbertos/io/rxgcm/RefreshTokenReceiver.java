package victoralbertos.io.rxgcm;

import android.util.Log;
import android.widget.Toast;

import rx.Observable;
import rx_gcm.GcmRefreshTokenReceiver;
import rx_gcm.TokenUpdate;

/**
 * Created by victor on 08/02/16.
 */
public class RefreshTokenReceiver implements GcmRefreshTokenReceiver {
    @Override public void onTokenReceive(Observable<TokenUpdate> oTokenUpdate) {
        oTokenUpdate.doOnError(error -> Log.e("RefreshTokenReceiver", error.getMessage()))
                .subscribe(tokenUpdate -> Toast.makeText(tokenUpdate.getApplication(), tokenUpdate.getToken(), Toast.LENGTH_LONG).show());
    }
}
