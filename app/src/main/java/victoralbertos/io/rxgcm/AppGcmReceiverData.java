package victoralbertos.io.rxgcm;

import android.os.Bundle;

import rx.Observable;
import rx_gcm.GcmReceiverData;
import rx_gcm.Message;
import victoralbertos.io.rxgcm.data.Cache;
import victoralbertos.io.rxgcm.data.api.GcmServerService;
import victoralbertos.io.rxgcm.data.entities.Notification;

/**
 * Created by victor on 01/04/16.
 */
public class AppGcmReceiverData implements GcmReceiverData {

    @Override public Observable<Message> onNotification(Observable<Message> oMessage) {
        return oMessage.doOnNext(message -> {
            Bundle payload = message.payload();

            String title = payload.getString(GcmServerService.TITLE);
            String body = payload.getString(GcmServerService.BODY);

            if (message.target().equals(GcmServerService.TARGET_ISSUE_GCM)) Cache.Pool.addIssue(new Notification(title, body));
            else if (message.target().equals(GcmServerService.TARGET_SUPPLY_GCM)) Cache.Pool.addSupply(new Notification(title, body));
        });
    }
}
