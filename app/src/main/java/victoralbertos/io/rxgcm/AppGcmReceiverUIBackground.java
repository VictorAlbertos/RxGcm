package victoralbertos.io.rxgcm;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import rx.Observable;
import rx_gcm.GcmReceiverUIBackground;
import rx_gcm.Message;
import victoralbertos.io.rxgcm.data.api.GcmServerService;
import victoralbertos.io.rxgcm.presentation.ActivityIssue;
import victoralbertos.io.rxgcm.presentation.HostActivitySupply;

/**
 * Created by victor on 01/04/16.
 */
public class AppGcmReceiverUIBackground implements GcmReceiverUIBackground {

    @Override public void onNotification(Observable<Message> oMessage) {
        oMessage.subscribe(this::buildAndShowNotification);
    }

    private void buildAndShowNotification(Message message) {
        backgroundMessage = message;

        Bundle payload = message.payload();
        Application application = message.application();

        String title = payload.getString(GcmServerService.TITLE);
        String body = payload.getString(GcmServerService.BODY);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(application)
                .setContentTitle(title)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntentForNotification(application, message));

        NotificationManager notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

    private PendingIntent getPendingIntentForNotification(Application application, Message message) {
        Class<? extends Activity> classActivity =  message.target().equals(GcmServerService.TARGET_ISSUE_GCM) ? ActivityIssue.class : HostActivitySupply.class;

        Intent resultIntent = new Intent(application, classActivity);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(application);
        stackBuilder.addParentStack(ActivityIssue.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    //Testing
    private static Message backgroundMessage;

    public static void initTestBackgroundMessage() {
        backgroundMessage = null;
    }

    public static Message getBackgroundMessage() {
        return backgroundMessage;
    }

}
