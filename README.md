[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxGcm-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3126)

RxGcm
=====
Every time I need to add Google cloud message to an Android application, the requirement of a confusing configuration always stops me 
to be happy doing it. 

That's why I've created RxGcm. I don't want to configure any [Service](http://developer.android.com/intl/es/guide/components/services.html), and I don't want to track the state of the application in order to know 
when to show a [Notification System](http://developer.android.com/intl/es/guide/topics/ui/notifiers/notifications.html) or an app inside message -or for that matters, which Activity/Fragment requires to be notified about the new received message. 
Plus, it seems to me that a reactive solution which allows subscribing for notifications fits almost perfectly in the very nature of push notifications.  

So, using RxGcm you do not need anymore to configure Android manifest, implementing Service(s) or keep track of your application state. And notifications will be
dispatch through a flexible and safety pipeline, I mean Observable(s) :)


RxGcm features:
--------------
* Not Android Manifest configuration
* Not need to implement Services
* Not need to track app's state: dispatching foreground and background notifications is handled for you
* Not worries about synchronizing threads or handling errors, which means Observable(s)

Setup
-----

Add RxGcm dependency and Google Services plugin to project level build.gradle.

```gradle
apply plugin: 'com.google.gms.google-services'

dependencies {
    compile 'com.github.VictorAlbertos:RxGcm:0.1.1'
    compile 'io.reactivex:rxjava:1.1.0'
}
```

Add Google Services to classpath and jitpack repository to root level build.gradle.

```gradle
dependencies {
    classpath 'com.google.gms:google-services:1.5.0'
}

allprojects {
    repositories {
        //..
        maven { url "https://jitpack.io" }
    }
}

```

There is, thought, one step behind which RxGcm can't do for you. You need to create a [google-services.json](https://developers.google.com/cloud-messaging/android/client) configuration file and place it in your Android application module. (You can create and download it from [here](https://developers.google.com/mobile/add?platform=android&cntapi=gcm&cnturl=https:%2F%2Fdevelopers.google.com%2Fcloud-messaging%2Fandroid%2Fclient&cntlbl=Continue%20Adding%20GCM%20Support&%3Fconfigured%3Dtrue))

Usage
=====
RxGcm functionality is access thought [RxGcm.Notifications](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/internal/RxGcm.java) enum, ensuring this way that only one instance of RxGcm will be create.

Register
--------
To listen for Ggm notifications, you just need to call [RxGcm.Notifications.register](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/internal/RxGcm.java#L78) in your Android Application class, passing as parameter the current Android Application instance.
 
```java
public class RxSampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        RxGcm.Notifications.register(this)
                .subscribe(new Subscriber<String>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable error) {
                    }

                    @Override public void onNext(String token) {
                    }
                });
    }
}
```

**Important:** The observable will not emit the token twice. It means that it will be emit the token only the first time 
RxGgm asks to Google for a token. But if the token is already cached, the observable will complete without emitting the item. 

Background notifications
------------------------
To listen for notifications which has been received when the application was on background, you need to call [RxGcm.Notifications.onBackgroundNotification](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/internal/RxGcm.java#L136), passing as parameter a class which
implements [GcmBackgroundReceiver](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/GcmBackgroundReceiver.java) interface:

```java
public class BackgroundMessageReceiver implements GcmBackgroundReceiver {

    @Override public void onMessage(Observable<BackgroundMessage> oMessage) {
        oMessage.subscribe(new Subscriber<BackgroundMessage>() {
            @Override public void onCompleted() {

            }

            @Override public void onError(Throwable e) {

            }

            @Override public void onNext(BackgroundMessage message) {
                //Build and show notification system
                Bundle payload = message.getPayload();
                Application application = message.getApplication();
                // NotificationCompat.Builder ...
            }
        });
    }
}
```

And in your Android Application class, call RxGcm.Notifications.onBackgroundNotification passing the BackgroundMessageReceiver implementation:

```java
public class RxSampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxGcm.Notifications.onBackgroundNotification(BackgroundMessageReceiver.class);
    }
}
```


Foreground notifications
------------------------
There are two ways to handle notifications when the app is on foreground state (it means its ui is visible to the user).

You can manage notifications in a centralized way:

```java
public class RxSampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        
        RxGcm.Notifications.onForegroundNotification().subscribe(new Subscriber<ForegroundMessage>() {
            @Override public void onCompleted() {}
        
            @Override public void onError(Throwable e) {}
        
            @Override public void onNext(ForegroundMessage foregroundMessage) {
                Activity activity = foregroundMessage.getCurrentActivity();
                Bundle payload = foregroundMessage.getPayload();
            }
        });
    }
}
```


Or you can adopt a more granular approach, implementing [GcmForegroundReceiver](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/GcmForegroundReceiver.java) interface in those Activities/Fragments which you want to subscribe for gcm notifications: 

```java
public class MainActivity extends AppCompatActivity implements GcmForegroundReceiver {


    @Override public void onReceiveMessage(Observable<ForegroundMessage> oMessage) {
        oMessage.subscribe(new Subscriber<ForegroundMessage>() {
            @Override public void onCompleted() {

            }

            @Override public void onError(Throwable e) {

            }

            @Override public void onNext(ForegroundMessage foregroundMessage) {
                Bundle payload = foregroundMessage.getPayload();
            }
        });
    }
}
```

```java
public class ChildFragment extends android.support.v4.app.Fragment implements GcmForegroundReceiver {

    @Override public void onReceiveMessage(Observable<ForegroundMessage> oMessage) {
        oMessage.subscribe(new Subscriber<ForegroundMessage>() {
            @Override public void onCompleted() {}

            @Override public void onError(Throwable e) {}

            @Override public void onNext(ForegroundMessage foregroundMessage) {
                Bundle payload = foregroundMessage.getPayload();
            }
        });
    }
    
}
```

**Limitation:**: Your fragments need to extend from android.support.v4.app.Fragment instead of android.app.Fragment, otherwise they will not be notified. 

You can, of course, combine both of them. 


Retrieving current token 
------------------------
If at some point you need to retrieve the gcm token device -e.g for updating the value on your server, you could do it easily calling [RxGcm.Notifications.currentToken](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/internal/RxGcm.java#L114):

```java
RxGcm.Notifications.currentToken().subscribe(new Subscriber<String>() {
    @Override public void onCompleted() {

    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(String token) {

    }
});
```


Subscribing for token updates
-----------------------------
As the [documentation](https://developers.google.com/android/reference/com/google/android/gms/iid/InstanceIDListenerService#onTokenRefresh) points out, the token device may need to be refreshed for some particular reason. 
In order to you be notified, you can call [RxGcm.Notifications.onRefreshToken](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/internal/RxGcm.java#L144), passing as parameter a class which implements [GcmRefreshTokenReceiver](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/GcmRefreshTokenReceiver.java) interface:

```java
public class RefreshTokenReceiver implements GcmRefreshTokenReceiver {
    @Override public void onTokenReceive(Observable<TokenUpdate> oTokenUpdate) {
        oTokenUpdate.subscribe(new Subscriber<TokenUpdate>() {
            @Override public void onCompleted() {
                
            }

            @Override public void onError(Throwable e) {

            }

            @Override public void onNext(TokenUpdate tokenUpdate) {

            }
        });
    }
}
```
 
And in your Android Application class, call RxGcm.Notifications.onRefreshToken passing the GcmRefreshTokenReceiver implementation:

```java
public class RxSampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxGcm.Notifications.onRefreshToken(RefreshTokenReceiver.class);
    }
}
```


Threading
---------
RxGcm uses internally [RxAndroid](https://github.com/ReactiveX/RxAndroid). Thanks to this, each observable created by RxGcm observes on the Main Thread and subscribe on an IO thread. 
This means you do not need to worry about threading and sync. But if you  need to change this behaviour, you can do it easily setting in which scheduler the observable needs to observe and subscribe.

Examples
--------
There is a complete example of RxGcm in the [app module](https://github.com/VictorAlbertos/RxGcm/tree/master/app). Plus, it has an integration test managed by [Espresso test kit](https://google.github.io/android-testing-support-library/) which show several uses cases.

Testing notification
--------------------
You can easily [send http post request](https://developers.google.com/cloud-messaging/downstream) to Google Cloud Messaging server using Postman or Advanced Rest Client.
Or you can send directly push notifications using [this page](http://1-dot-sigma-freedom-752.appspot.com/gcmpusher.jsp). 

Author
-------
**Víctor Albertos**

* <https://twitter.com/_victorAlbertos>
* <https://linkedin.com/in/victoralbertos>
* <https://github.com/VictorAlbertos>

Another author's libraries using RxJava:
----------------------------------------
* [RxCache](https://github.com/VictorAlbertos/RxCache): Reactive caching library for Android and Java. 


