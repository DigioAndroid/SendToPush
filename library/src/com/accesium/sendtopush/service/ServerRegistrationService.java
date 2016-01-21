package com.accesium.sendtopush.service;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.accesium.sendtopush.datatypes.Environment;
import com.accesium.sendtopush.datatypes.ServerResult;
import com.accesium.sendtopush.util.Constants;
import com.accesium.sendtopush.util.Utils;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Fran Gilberte on 20/01/2016.
 */
public class ServerRegistrationService extends BaseService {
    private final String ENABLED = "enabled";
    private Service mRunService;

    public ServerRegistrationService(String baseUrl) {
        this.mRunService = getAdapter(baseUrl).create(Service.class);
    }

    public Observable<ServerResult> registerInServer(String apiKey, String company, String appName, String token, String appUsername,
                                                     String appVersion, String uid, Environment environment, List<String> tags) {

        return mRunService.register(apiKey, company, appName, Constants.TASK_REGISTER, token, appUsername, appVersion, uid,
                android.os.Build.MODEL.toString(),
                "TerminalPruebas",
                String.valueOf(android.os.Build.VERSION.SDK_INT),
                ENABLED, ENABLED, ENABLED, environment.getEnvironment(), tags);
    }

    public Observable<ServerResult> unregisterInServer(String apikey, String company, String appName, String userPid) {
        return mRunService.unregister(apikey, company, appName, userPid, Constants.TASK_UNREGISTER_ID);
    }


    interface Service {
        @GET("gcm")
        Observable<ServerResult> register(
                @Query(Constants.TASK_APIKEY) String apiKey,
                @Query(Constants.TASK_COMPANY) String company,
                @Query(Constants.TASK_APPNAME) String appName,
                @Query(Constants.TASK) String task,
                @Query(Constants.TASK_DEVICE_TOKEN) String token,
                @Query(Constants.TASK_APP_USERNAME) String user,
                @Query(Constants.TASK_APP_VERSION) String version,
                @Query(Constants.TASK_DEVICE_UID) String uid,
                @Query(Constants.TASK_DEVICE_NAME) String name,
                @Query(Constants.TASK_DEVICE_MODEL) String model,
                @Query(Constants.TASK_DEVICE_VERSION) String deviceVersion,
                @Query(Constants.TASK_PUSH_BADGE) String badge,
                @Query(Constants.TASK_PUSH_ALERT) String alert,
                @Query(Constants.TASK_PUSH_SOUND) String sound,
                @Query(Constants.TASK_ENVIRONMENT) String environment,
                @Query(Constants.TASK_TAGS) List<String> tags);

        @GET("gcm")
        Observable<ServerResult> unregister(
                @Query(Constants.TASK_APIKEY) String apiKey,
                @Query(Constants.TASK_COMPANY) String company,
                @Query(Constants.TASK_APPNAME) String appName,
                @Query(Constants.TASK_PID) String pid,
                @Query(Constants.TASK) String task);
    }


}
