/*
 * Copyright 2016 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx_gcm.internal;


import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx_gcm.BackgroundMessage;
import rx_gcm.ForegroundMessage;
import rx_gcm.GcmBackgroundReceiver;
import rx_gcm.GcmForegroundReceiver;
import rx_gcm.GcmRefreshTokenReceiver;
import rx_gcm.TokenUpdate;
import victoralbertos.io.rx_gcm.R;

/**
 * Ensures a single instance of RxGcm for the Application
 */
public enum RxGcm {
    Notifications;

    private Subscriber<? super ForegroundMessage> onForegroundNotificationSubscriber;
    private ActivitiesLifecycleCallbacks activitiesLifecycle;
    private GetGcmServerToken getGcmServerToken;
    private Persistence persistence;
    private GetGcmForegroundReceivers getGcmForegroundReceivers;
    private Scheduler mainThreadScheduler;
    private boolean testing;

    //VisibleForTesting
    void initForTesting(GetGcmServerToken getGcmServerToken, Persistence persistence, ActivitiesLifecycleCallbacks activitiesLifecycle, GetGcmForegroundReceivers getGcmForegroundReceivers) {
        this.testing = true;
        this.getGcmServerToken = getGcmServerToken;
        this.persistence = persistence;
        this.activitiesLifecycle = activitiesLifecycle;
        this.getGcmForegroundReceivers = getGcmForegroundReceivers;
        this.mainThreadScheduler = Schedulers.io();
    }

    private void init() {
        if (testing) return;
        getGcmServerToken = new GetGcmServerToken();
        persistence = new Persistence();
        getGcmForegroundReceivers = new GetGcmForegroundReceivers();
        mainThreadScheduler = AndroidSchedulers.mainThread();
    }

    /**
     * Register the device on Google Cloud Messaging server.
     * The observable will not emit the token twice. It means that it will be emit the token only the first time. RxGgm asks to Google for a token.
     * @param application The Android Application class
     */
    public Observable<String> register(final Application application) {
        init();

        activitiesLifecycle = new ActivitiesLifecycleCallbacks(application);

        Observable.OnSubscribe<String> onSubscribe = new Observable.OnSubscribe<String>() {
            @Override public void call(Subscriber<? super String> subscriber) {
                Context context = activitiesLifecycle.getApplication();

                String token = persistence.getToken(context);
                if (token != null) {
                    subscriber.onCompleted();
                    return;
                }

                try {
                    token = getGcmServerToken.retrieve(context);
                    persistence.saveToken(token, context);
                    subscriber.onNext(token);
                } catch (final Exception e) {
                    subscriber.onError(e);
                }

                subscriber.onCompleted();
            }
        };

        return Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
                .retryWhen(new RetryWithDelay(3, 2000));
    }

    /**
     * @return Current token associated with the device on Google Cloud Messaging serve.
     */
    public Observable<String> currentToken() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String token = persistence.getToken(activitiesLifecycle.getApplication());
                if (token != null) {
                    subscriber.onNext(token);
                } else {
                    subscriber.onError(new RuntimeException(Constants.ERROR_NOT_CACHED_TOKEN_AVAILABLE));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
                .retryWhen(new RetryWithDelay(3, 2000));
    }

    /**
     * Sets the class for listening background messages.
     * @param aClass The class which implements GcmBackgroundReceiver and so the class which will be notified when a message has been received
     * @see GcmBackgroundReceiver
     */
    public <T extends GcmBackgroundReceiver> void onBackgroundNotification(Class<T> aClass) {
        persistence.saveClassNameGcmBackgroundReceiver(aClass.getName(), activitiesLifecycle.getApplication());
    }

    /**
     * @param aClass The class which implements GcmRefreshTokenReceiver and so the class which will be notified when a token refresh happens.
     * @see GcmRefreshTokenReceiver
     */
    public <T extends GcmRefreshTokenReceiver> void onRefreshToken(Class<T> aClass) {
        persistence.saveClassNameGcmRefreshTokenReceiver(aClass.getName(), activitiesLifecycle.getApplication());
    }

    /**
     * To subscribe for foreground messages
     * @return An observable which emits messages received from Gcm meanwhile the app is on foreground state.
     */
    public Observable<ForegroundMessage> onForegroundNotification() {
        return Observable.create(new Observable.OnSubscribe<ForegroundMessage>() {
            @Override public void call(Subscriber<? super ForegroundMessage> subscriber) {
                onForegroundNotificationSubscriber = subscriber;
            }
        }).subscribeOn(Schedulers.io()).observeOn(mainThreadScheduler);
    }

    void onTokenRefreshed() {
        String newToken;
        Observable oExceptionGcmServer;
        try {
            newToken = getGcmServerToken.retrieve(activitiesLifecycle.getApplication());
            persistence.saveToken(newToken, activitiesLifecycle.getApplication());
            oExceptionGcmServer = null;
        } catch (final Exception exception) {
            newToken = null;
            oExceptionGcmServer = Observable.create(new Observable.OnSubscribe<Object>() {
                @Override public void call(Subscriber<? super Object> subscriber) {
                    subscriber.onError(new RuntimeException(exception.getMessage()));
                }
            });
        }

        String className = persistence.getClassNameGcmRefreshTokenReceiver(activitiesLifecycle.getApplication());
        if (className == null) {
            Log.w(getAppName(), Constants.NOT_RECEIVER_FOR_REFRESH_TOKEN);
            return;
        }

        GcmRefreshTokenReceiver tokenReceiver = getInstanceClassByName(className);
        if (newToken != null) {
            TokenUpdate tokenUpdate = new TokenUpdate(newToken, activitiesLifecycle.getApplication());
            tokenReceiver.onTokenReceive(Observable.just(tokenUpdate));
        } else {
            tokenReceiver.onTokenReceive(oExceptionGcmServer);
        }
    }

    void onNotificationReceived(String from, Bundle payload) {
        Application application = activitiesLifecycle.getApplication();

        if (activitiesLifecycle.isAppOnBackground()) {
            BackgroundMessage message = new BackgroundMessage(application, from, payload);
            notifyBackgroundMessage(message);
        } else {
            ForegroundMessage message = new ForegroundMessage(activitiesLifecycle.getLiveActivityOrNull(), from, payload);
            notifyForegroundMessage(message);
        }
    }

    private void notifyBackgroundMessage(BackgroundMessage message) {
        String className = persistence.getClassNameGcmBackgroundReceiver(activitiesLifecycle.getApplication());
        if (className == null) {
            Log.w(getAppName(), Constants.NOT_RECEIVER_FOR_BACKGROUND_NOTIFICATIONS);
            return;
        }

        GcmBackgroundReceiver gcmBackgroundReceiver = getInstanceClassByName(className);
        Observable<BackgroundMessage> oNotification = Observable.just(message).observeOn(mainThreadScheduler);
        gcmBackgroundReceiver.onMessage(oNotification);
    }

    private void notifyForegroundMessage(ForegroundMessage message) {
        if (onForegroundNotificationSubscriber != null){
            onForegroundNotificationSubscriber.onNext(message);
        }

        Observable<ForegroundMessage> observable = Observable.just(message).observeOn(mainThreadScheduler);
        ConnectableObservable connectableObservable = observable.publish();

        List<GcmForegroundReceiver> foregroundReceivers = getGcmForegroundReceivers.retrieve(activitiesLifecycle.getLiveActivityOrNull());
        for (GcmForegroundReceiver foregroundReceiver : foregroundReceivers) {
            foregroundReceiver.onReceiveMessage(connectableObservable);
        }

        connectableObservable.connect();
    }

    <T> T getInstanceClassByName(String className) {
        try {
            Class<T> clazz = (Class<T>) Class.forName(className);
            T instance = clazz.newInstance();
            return instance;
        } catch (Exception e) {
            String error = Constants.ERROR_NOT_PUBLIC_EMPTY_CONSTRUCTOR_FOR_CLASS;
            error = error.replace("$$$", className);
            throw new IllegalStateException(error);
        }
    }

    private String getAppName() {
        return activitiesLifecycle.getApplication().getString(R.string.app_name);
    }
}
