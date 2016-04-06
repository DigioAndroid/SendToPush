package com.accesium.sendtopush.service;

import com.accesium.sendtopush.tools.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Fran Gilberte on 19/01/2016.
 */
public class GcmRegistrationService {
    private static GoogleCloudMessaging gcm;

    public GcmRegistrationService(GoogleCloudMessaging gcm) {
        this.gcm = gcm;
    }

    public Observable<String> register(String gcmSenderId) {
        return Observable.create((Subscriber<? super String> subscriber) -> {
            try {
                if (!subscriber.isUnsubscribed()) {
                    String token = gcm.register(gcmSenderId);

                    subscriber.onNext(token);
                    Log.d("Gcm register success");

                    subscriber.onCompleted();
                }
            } catch (IOException e) {
                subscriber.onError(e);
                Log.d("Gcm register failed");
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Boolean> unregister() {
        return Observable.create((Subscriber<? super Boolean> subscriber) -> {
            try {
                if (!subscriber.isUnsubscribed()) {
                    gcm.unregister();
                    subscriber.onNext(true);
                    Log.d("Gcm unregister success");
                    subscriber.onCompleted();
                }
            } catch (IOException e) {
                subscriber.onError(e);
                Log.d("Gcm unregister failed");
            }
        }).subscribeOn(Schedulers.io());
    }

}
