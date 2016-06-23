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

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.schedulers.Schedulers;
import rx_gcm.GcmReceiverData;
import rx_gcm.GcmReceiverUIBackground;
import rx_gcm.GcmRefreshTokenReceiver;
import rx_gcm.Message;
import rx_gcm.TokenUpdate;
import victoralbertos.io.rx_gcm.R;

/**
 * Ensures a single instance of RxGcm for the Application
 */
public enum RxGcm {
    Notifications;

    private final static String RX_GCM_KEY_TARGET = "rx_gcm_key_target"; //VisibleForTesting
    private ActivitiesLifecycleCallbacks activitiesLifecycle;
    private GetGcmServerToken getGcmServerToken;
    private Persistence persistence;
    private GetGcmReceiversUIForeground getGcmReceiversUIForeground;
    private Scheduler mainThreadScheduler;
    private boolean testing;

    //VisibleForTesting
    void initForTesting(GetGcmServerToken getGcmServerToken, Persistence persistence, ActivitiesLifecycleCallbacks activitiesLifecycle, GetGcmReceiversUIForeground getGcmReceiversUIForeground) {
        this.testing = true;
        this.getGcmServerToken = getGcmServerToken;
        this.persistence = persistence;
        this.activitiesLifecycle = activitiesLifecycle;
        this.getGcmReceiversUIForeground = getGcmReceiversUIForeground;
        this.mainThreadScheduler = Schedulers.io();
    }

    void init(Application application) {
        if (testing || activitiesLifecycle != null) return;
        getGcmServerToken = new GetGcmServerToken();
        persistence = new Persistence();
        getGcmReceiversUIForeground = new GetGcmReceiversUIForeground();
        mainThreadScheduler = AndroidSchedulers.mainThread();
        activitiesLifecycle = new ActivitiesLifecycleCallbacks(application);
    }

    /**
     * Register the device on Google Cloud Messaging server and set the class for listening messages.
     * The observable will not emit the token twice. It means that it will be emit the token only the first time. RxGgm asks to Google for a token.
     * @param application The Android Application class.
     * @param gcmReceiverClass The class which implements GcmReceiver.
     * @see GcmReceiverData
     */
    public <T extends GcmReceiverData, U extends GcmReceiverUIBackground> Observable<String> register(final Application application, final Class<T> gcmReceiverClass, final Class<U> gcmReceiverUIBackgroundClass) {
        init(application);

        Observable.OnSubscribe<String> onSubscribe = new Observable.OnSubscribe<String>() {
            @Override public void call(Subscriber<? super String> subscriber) {
                Context context = activitiesLifecycle.getApplication();
                persistence.saveClassNameGcmReceiverAndGcmReceiverUIBackground(gcmReceiverClass.getName(), gcmReceiverUIBackgroundClass.getName() ,context);

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
        });
    }

    /**
     * @param aClass The class which implements GcmRefreshTokenReceiver and so the class which will be notified when a token refresh happens.
     * @see GcmRefreshTokenReceiver
     */
    public <T extends GcmRefreshTokenReceiver> void onRefreshToken(Class<T> aClass) {
        persistence.saveClassNameGcmRefreshTokenReceiver(aClass.getName(), activitiesLifecycle.getApplication());
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
        String target = payload != null ? payload.getString(RX_GCM_KEY_TARGET, null) : "";

        Observable<Message> oMessage = Observable.just(new Message(from, payload, target, application));

        String className = persistence.getClassNameGcmReceiver(activitiesLifecycle.getApplication());
        GcmReceiverData gcmReceiverData = getInstanceClassByName(className);

        gcmReceiverData.onNotification(oMessage)
                .doOnNext(new Action1<Message>() {
                    @Override public void call(Message message) {
                        if (activitiesLifecycle.isAppOnBackground()) {
                            notifyGcmReceiverBackgroundMessage(message);
                        } else {
                            notifyGcmReceiverForegroundMessage(message);
                        }
                    }
                })
                .subscribe(Actions.empty(), new Action1<Throwable>() {
                    @Override public void call(Throwable throwable) {
                        String message = "Error thrown from GcmReceiverData subscription. Cause exception: " + throwable.getMessage();
                        Log.e("RxGcm", message);
                    }
                });
    }

    private void notifyGcmReceiverBackgroundMessage(Message message) {
        String className = persistence.getClassNameGcmReceiverUIBackground(activitiesLifecycle.getApplication());
        final GcmReceiverUIBackground gcmReceiverUIBackground = getInstanceClassByName(className);

        Observable<Message> oNotification = Observable.just(message)
            .observeOn(mainThreadScheduler);
        gcmReceiverUIBackground.onNotification(oNotification);
    }

    private void notifyGcmReceiverForegroundMessage(Message message) {
        String className = persistence.getClassNameGcmReceiver(activitiesLifecycle.getApplication());

        if (className == null) {
            Log.w(getAppName(), Constants.NOT_RECEIVER_FOR_FOREGROUND_UI_NOTIFICATIONS);
            return;
        }

        final GetGcmReceiversUIForeground.Wrapper wrapperGcmReceiverUIForeground = getGcmReceiversUIForeground.retrieve(message.target(), activitiesLifecycle.getLiveActivityOrNull());
        if (wrapperGcmReceiverUIForeground == null) return;

        Observable<Message> oNotification = Observable.just(message)
            .observeOn(mainThreadScheduler);

        if (wrapperGcmReceiverUIForeground.isTargetScreen()) {
            wrapperGcmReceiverUIForeground.gcmReceiverUIForeground()
                .onTargetNotification(oNotification);
        } else {
            wrapperGcmReceiverUIForeground.gcmReceiverUIForeground()
                .onMismatchTargetNotification(oNotification);
        }
    }

    <T> Class<T> getClassByName(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    <T> T getInstanceClassByName(String className) {
        try {
            T instance = (T) getClassByName(className).newInstance();
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
