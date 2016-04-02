package victoralbertos.io.rxgcm.data.api;

import retrofit.Response;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by victor on 08/02/16.
 */
public interface ApiGcmServer {
    String URL_BASE = "https://gcm-http.googleapis.com";
    String API_KEY = "Authorization: key=AIzaSyDQryfz0OXDnae5yPyyTO9dg4BmZggCkM8";
    String CONTENT_TYPE = "Content-Type: application/json";

    @Headers({API_KEY, CONTENT_TYPE})
    @POST("/gcm/send")
    Observable<Response<GcmServerService.GcmResponseServer>> sendNotification(@Body GcmServerService.Payload payload);
}