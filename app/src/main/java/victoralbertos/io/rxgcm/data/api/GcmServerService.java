package victoralbertos.io.rxgcm.data.api;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx_gcm.internal.RxGcm;

/**
 * Created by victor on 08/02/16.
 */
public class GcmServerService {
    private final ApiGcmServer apiGcmServer;
    public final static String TARGET_ISSUE_GCM = "target_issue_gcm";
    public final static String TARGET_SUPPLY_GCM = "target_supply_gcm";
    public final static String TARGET_NESTED_SUPPLY_GCM = "target_nested_supply_gcm";
    public final static String TITLE = "title", BODY = "body";

    public GcmServerService() {
        this.apiGcmServer = new Retrofit.Builder()
                .baseUrl(ApiGcmServer.URL_BASE)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiGcmServer.class);
    }

    public Observable<Boolean> sendGcmNotificationRequestingIssue(String title, String body) {
        return RxGcm.Notifications.currentToken()
                .map(token -> new Payload(token, title, body, TARGET_ISSUE_GCM))
                .concatMap(payload -> apiGcmServer.sendNotification(payload))
                .map(gcmResponseServerResponse -> gcmResponseServerResponse.body().success())
                .onErrorReturn(throwable -> false);
    }

    public Observable<Boolean> sendGcmNotificationRequestingSupply(String title, String body) {
        return RxGcm.Notifications.currentToken()
                .map(token -> new Payload(token, title, body, TARGET_SUPPLY_GCM))
                .concatMap(payload -> apiGcmServer.sendNotification(payload))
                .map(gcmResponseServerResponse -> gcmResponseServerResponse.body().success())
                .onErrorReturn(throwable -> false);
    }

    public Observable<Boolean> sendGcmNotificationRequestingNestedSupply(String title, String body) {
        return RxGcm.Notifications.currentToken()
                .map(token -> new Payload(token, title, body, TARGET_NESTED_SUPPLY_GCM))
                .concatMap(payload -> apiGcmServer.sendNotification(payload))
                .map(gcmResponseServerResponse -> gcmResponseServerResponse.body().success())
                .onErrorReturn(throwable -> false);
    }

    static class GcmResponseServer {
        private final int success;

        public GcmResponseServer(int success) {
            this.success = success;
        }

        boolean success() {
            return success != 0;
        }
    }

    static class Payload {
        private final String to;
        private Notification data;

        public Payload(String to, String title, String body, String target) {
            this.to = to;
            data = new Notification(title, body, target);
        }

        private class Notification {
            private final String title, body, rx_gcm_key_target;

            public Notification(String title, String body, String target) {
                this.title = title;
                this.body = body;
                this.rx_gcm_key_target = target;
            }
        }

    }

}
