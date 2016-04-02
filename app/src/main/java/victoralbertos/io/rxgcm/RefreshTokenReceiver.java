package victoralbertos.io.rxgcm;

import rx.Observable;
import rx_gcm.GcmRefreshTokenReceiver;
import rx_gcm.TokenUpdate;

/**
 * Created by victor on 08/02/16.
 */
public class RefreshTokenReceiver implements GcmRefreshTokenReceiver {

    @Override public void onTokenReceive(Observable<TokenUpdate> oTokenUpdate) {
        oTokenUpdate.subscribe(tokenUpdate -> {}, error -> {});
    }

}
