package rx_gcm;

import rx.Observable;

/**
 * The class which implements this interface will receive messages when application is on background state.
 */
public interface GcmBackgroundReceiver {
    void onMessage(Observable<BackgroundMessage> oMessage);
}
