package victoralbertos.io.rxgcm;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import rx.Observable;
import rx_gcm.BackgroundMessage;
import rx_gcm.GcmBackgroundReceiver;
import victoralbertos.io.rxgcm.presentation.MainActivity;

/**
 * Created by victor on 08/02/16.
 */
public class BackgroundMessageReceiver implements GcmBackgroundReceiver {
    public final static String TITLE = "title", BODY = "body";

    @Override public void onMessage(Observable<BackgroundMessage> oMessage) {
        oMessage.doOnError(error -> Log.e("BackgroundMessageReceiver", error.getMessage()))
                .subscribe(message -> buildAndShowNotification(message));
    }

    private void buildAndShowNotification(BackgroundMessage message) {
        backgroundMessages = message;

        Bundle payload = message.getPayload();
        Application application = message.getApplication();

        String title = payload.getString(TITLE);
        String body = payload.getString(BODY);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(application)
                .setContentTitle(title)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntentForNotification(application));

        NotificationManager notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

    private PendingIntent getPendingIntentForNotification(Application application) {
        Intent resultIntent = new Intent(application, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(application);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    //Testing
    private static BackgroundMessage backgroundMessages;

    public static void initTestBackgroundMessages() {
        backgroundMessages = null;
    }

    public static BackgroundMessage getBackgroundMessages() {
        return backgroundMessages;
    }
}
