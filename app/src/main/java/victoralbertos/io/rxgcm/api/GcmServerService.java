package victoralbertos.io.rxgcm.api;

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

    public GcmServerService() {
        this.apiGcmServer = new Retrofit.Builder()
                .baseUrl(ApiGcmServer.URL_BASE)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiGcmServer.class);
    }

    public Observable<Boolean> sendGcmNotification(String title, String body) {
        return RxGcm.Notifications.currentToken()
                .map(token -> new Payload(token, title, body))
                .concatMap(payload -> apiGcmServer.sendNotification(payload))
                .map(gcmResponseServerResponse -> gcmResponseServerResponse.body())
                .map(gcmResponseServer -> gcmResponseServer.success())
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

        public Payload(String token, String title, String body) {
            to = token;
            data = new Notification(title, body);
        }

        private class Notification {
            private final String title, body;

            public Notification(String title, String body) {
                this.title = title;
                this.body = body;
            }
        }

    }

}
