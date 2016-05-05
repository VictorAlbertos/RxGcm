[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxGcm-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3126)

# RxGcm
RxJava extension for Gcm which acts as an architectural approach to easily satisfy the requirements of an android app when dealing with push notifications.

## Features:
* Remove android boilerplate code (not need for `Manifest` or `Service(s)` configuration).
* Decouple presentation responsibilities from data responsibilities when receiving notifications.
* Deploy a targeting strategy to aim for the desired Activity/Fragment when receiving notifications.

## Setup
Add RxGcm dependency and Google Services plugin to project level build.gradle.

```gradle
apply plugin: 'com.google.gms.google-services'

dependencies {
    compile 'com.github.VictorAlbertos:RxGcm:0.2.2'
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

## Usage

### GcmReceiverData
[GcmReceiverData](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/GcmReceiverData.java) implementation should be responsible for **updating the data models**. The `onNotification` method requires to return an instance of the `observable` supplied as argument, after applying `doOnNext` operator to perform the update action: 

```java
public class AppGcmReceiverData implements GcmReceiverData {    
 
 	@Override public Observable<Message> onNotification(Observable<Message> oMessage) {         
 		return oMessage.doOnNext(message -> {});     
 	} 
 	
 }
```

The `observable` type is an instance of [Message](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/Message.java), which holds a reference to the android `Application` instance, the `Bundle` notification and a method called `target()`, which returns the key associated with this notification.  

```java
public class AppGcmReceiverData implements GcmReceiverData {

    @Override public Observable<Message> onNotification(Observable<Message> oMessage) {
        return oMessage.doOnNext(message -> {
            Bundle payload = message.payload();

            String title = payload.getString("title");
            String body = payload.getString("body");

            if (message.target().equals("issues")) SimpleCache.addIssue(new Notification(title, body));
            else if (message.target().equals("supplies")) SimpleCache.addSupply(new Notification(title, body));
        });
    }
    
} 
```

To RxGcm be able to return a not null `string` value when calling `target()` method, you need to add the key rx_gcm_key_target to the payload of the push notification: 
 
```json            
{ 
  "data": {
    "title":"A title 4",
    "body":"A body 4",
    "rx_gcm_key_target":"supplies"
  },
  "to":"token_device"
  }
}
```

If rx_gcm_key_target is not added to the json payload, you will get a null value when calling the `target()` method. So, you can ignore this, but you would be missing the benefits of the targeting strategy.

### GcmReceiverUIBackground and GcmReceiverUIForeground
Both of them will be called only after `GcmReceiverData` `observable` has reached `onCompleted()` state. This way it’s safe to assume that any operation related to updating the data model has been successfully achieved, and now it’s time to reflect these updates in the presentation layer. 

#### GcmReceiverUIBackground
`GcmReceiverUIBackground` implementation will be called when a notification is received and the application is in the background. Probably the implementation class will be responsable for building and showing system notifications. 

```java
public class AppGcmReceiverUIBackground implements GcmReceiverUIBackground {   
   
	@Override public void onNotification(Observable<Message> oMessage) {         
		oMessage.subscribe(message -> buildAndShowNotification(message));     
	}
	
}
```

#### GcmReceiverUIForeground
`GcmReceiverUIForeground` implementation will be called when a notification is received and the application is in the foreground. The implementation class must be an `Activity` or an `android.support.v4.app.Fragment`. `GcmReceiverUIForeground` exposes a method called `target()`, which forces to the implementation class to return a string. 

RxGcm internally compares this string to the value of the rx_gcm_key_target node payload notification. If the current `Activity` or visible `Fragment` `target()` method value matches with the one of rx_gcm_key_target node, `onTargetNotification()` method will be called, otherwise `onMismatchTargetNotification()` method will be called. 

```java
public abstract class BaseFragment extends android.support.v4.app.Fragment implements GcmReceiverUIForeground {      
	
    @Override public void onMismatchTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            showAlert(message);
        });
    }  
	  
}
```

```java
public class FragmentIssues extends BaseFragment {      
	
    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }   

	@Override public String target() {         
		return "issues";     
	}    
	  
}
```

```java
public class FragmentSupplies extends android.support.v4.app.Fragment implements GcmReceiverUIForeground {      
	
    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }    

	@Override public String target() {         
		return "supplies";     
	}    
	  
}
```

**Limitation:**: Your fragments need to extend from `android.support.v4.app.Fragment` instead of `android.app.Fragment`, otherwise they won't be notified. 

### RefreshTokenReceiver
[GcmRefreshTokenReceiver](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/GcmRefreshTokenReceiver.java) implementation will be called when the token has been updated. As the [documentation](https://developers.google.com/android/reference/com/google/android/gms/iid/InstanceIDListenerService#onTokenRefresh) points out, the token device may need to be refreshed for some particular reason. 

```java
public class RefreshTokenReceiver implements GcmRefreshTokenReceiver {
    
    @Override public void onTokenReceive(Observable<TokenUpdate> oTokenUpdate) {
        oTokenUpdate.subscribe(tokenUpdate -> {}, error -> {});
    }
    
}
```
 
### Retrieving current token 
If at some point you need to retrieve the gcm token device -e.g for updating the value on your server, you could do it easily calling [RxGcm.Notifications.currentToken](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/internal/RxGcm.java#L114):

```java
    RxGcm.Notifications.currentToken().subscribe(token -> {}, error -> {});
```


### Register RxGcm classes
Once you have implemented `GcmReceiverData` and `GcmReceiverUIBackground` interfaces is time to register them in your Android `Application` class calling [RxGcm.Notifications.register](https://github.com/VictorAlbertos/RxGcm/blob/master/rx_gcm/src/main/java/rx_gcm/internal/RxGcm.java#L79). Plus, register `RefreshTokenReceiver` implementation too at this point. 
   
```java
public class RxSampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        RxGcm.Notifications.register(this, AppGcmReceiverData.class, AppGcmReceiverUIBackground.class)
                .subscribe(token -> {}, error -> {});   
                
        RxGcm.Notifications.onRefreshToken(RefreshTokenReceiver.class);
    }

}
```

**Important:** The `observable` returned by `RxGcm.Notifications.register()`method will not emit the token twice. It means that it will be emit the token only the first time 
RxGgm asks to Google for a token. But if the token is already cached, the observable will complete without emitting the item. 

### Threading
RxGcm uses internally [RxAndroid](https://github.com/ReactiveX/RxAndroid). Thanks to this, each observable created by RxGcm observes on the Main Thread and subscribe on an IO thread. 
This means you do not need to worry about threading and sync. But if you  need to change this behaviour, you can do it easily setting in which scheduler the observable needs to observe and subscribe.

## Examples
There is a complete example of RxGcm in the [app module](https://github.com/VictorAlbertos/RxGcm/tree/master/app). Plus, it has an integration test managed by [Espresso test kit](https://google.github.io/android-testing-support-library/) which show several uses cases.

## Testing notification
You can easily [send http post request](https://developers.google.com/cloud-messaging/downstream) to Google Cloud Messaging server using Postman or Advanced Rest Client.
Or you can send directly push notifications using [this page](http://1-dot-sigma-freedom-752.appspot.com/gcmpusher.jsp). 

## Author
**Víctor Albertos**
* <https://twitter.com/_victorAlbertos>
* <https://linkedin.com/in/victoralbertos>
* <https://github.com/VictorAlbertos>

## Another author's libraries using RxJava:
* [RxCache](https://github.com/VictorAlbertos/RxCache): Reactive caching library for Android and Java.
* [RxPaparazzo](https://github.com/FuckBoilerplate/RxPaparazzo): RxJava extension for Android to take images using camera and gallery.
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A reactive-tiny-badass-vindictive library to break with the OnActivityResult implementation as it breaks the observables chain. 


