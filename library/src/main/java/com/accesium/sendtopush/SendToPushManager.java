package com.accesium.sendtopush;

import android.content.Context;
import android.content.SharedPreferences;

import com.accesium.sendtopush.datatypes.Environment;
import com.accesium.sendtopush.datatypes.Preferences;
import com.accesium.sendtopush.datatypes.PushError;
import com.accesium.sendtopush.datatypes.PushError.Type;
import com.accesium.sendtopush.datatypes.PushStateType;
import com.accesium.sendtopush.datatypes.ServerResult;
import com.accesium.sendtopush.listeners.PushResponseListener;
import com.accesium.sendtopush.service.GcmRegistrationService;
import com.accesium.sendtopush.service.ServerRegistrationService;
import com.accesium.sendtopush.tools.Log;
import com.accesium.sendtopush.util.Constants;
import com.accesium.sendtopush.util.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Class that encapsulate the register and unregister in push server. It handles
 * the registration in Google's server, querying a push token. After this, it is
 * registered within our push server by sending the required parameters.
 *
 * @author Isidoro Castell
 */
public class SendToPushManager {

    private static SendToPushManager sInstance;

    private final String apiKey;
    private final String company;
    private final String appname;
    private final String gcmSenderId;
    private final Environment environment;
    private String appUsername;
    private ArrayList<String> tags;
    private PushResponseListener listener;
    private boolean forceRegister;

    /**
     * The constructor
     *
     * @param apiKey      The generated apikey in the push server
     * @param company     Your company ID
     * @param appname     APP ID in the push server
     * @param gcmSenderId The GCM Sender ID
     */
    private SendToPushManager(String apiKey, String company, String appname, String gcmSenderId, Environment environment) {
        this.apiKey = apiKey;
        this.company = company;
        this.appname = appname;
        this.gcmSenderId = gcmSenderId;
        this.environment = environment;
    }

    /**
     * Return, in case of it exists, an instance of SendToPushManager. <b>This
     * call has to be done before
     * init(String, String, String, String)},</b> otherwise an exception will be
     * launched.
     *
     * @return The instance of the object
     * @throws IllegalStateException If the object has not been initialized
     */
    public static SendToPushManager getInstance() throws IllegalStateException {
        if (sInstance == null) {
            throw new IllegalStateException("not initialized");
        }
        return sInstance;
    }

    /**
     * Initialize the object, in case of it not exists, with the given
     * parameters obtained from the server. If we have already initialized the
     * object the old instance will be returned.
     *
     * @param apiKey      Generated in the push server, when the project has been
     *                    register
     * @param company     Company registered in the push server
     * @param appname     Name of the application in the push server
     * @param gcmSenderId The GCM Sender ID obtained from Google APIs Console page
     * @param environment The environment which the application is executed
     * @return An instance of the object created
     */
    public static SendToPushManager init(String apiKey, String company, String appname, String gcmSenderId, Environment environment) {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (SendToPushManager.class) {
            if (sInstance == null) {
                sInstance = new SendToPushManager(apiKey, company, appname, gcmSenderId, environment);
            }
        }
        return sInstance;
    }

    public static boolean isInitialized() {
        return sInstance != null;
    }

    /**
     * Enable or disable the logging in the library
     *
     * @param enable Whether enable or not
     */
    public void enableDebug(boolean enable) {
        Log.initialize(enable);
    }

    /**
     * @return the appUsername
     */
    public String getAppUsername() {
        return appUsername;
    }

    /**
     * @param appUsername the appUsername to set
     */
    public void setAppUsername(String appUsername) {
        this.appUsername = appUsername;
    }

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    /**
     * @return the listener
     */
    public PushResponseListener getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(PushResponseListener listener) {
        this.listener = listener;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @return the company
     */
    public String getCompany() {
        return company;
    }

    /**
     * @return the appname
     */
    public String getAppname() {
        return appname;
    }

    /**
     * @return the gcmSenderId
     */
    public String getGcmSenderId() {
        return gcmSenderId;
    }

    /**
     * Start the registration process. First it queries the GCM token, once this
     * is done it is registered in our push server.
     *
     * @param context     The application context
     * @param appUsername The user name to save in our push server. This will be used to
     *                    send push messages.
     * @param listener    The callback to receive if the query was successful or not.
     */
    public void register(Context context, String appUsername, PushResponseListener listener) {
        register(context, appUsername, listener, null);
    }

    /**
     * Start the registration process. First it queries the GCM token, once this
     * is done it is registered in our push server.
     *
     * @param context     The application context
     * @param appUsername The user name to save in our push server. This will be used to
     *                    send push messages.
     * @param listener    The callback to receive if the query was successful or not.
     * @param tags        Tags list used to associate a device with a specific group
     */
    public void register(Context context, String appUsername, PushResponseListener listener, ArrayList<String> tags) {
        register(context, appUsername, listener, tags, false);
    }

    public void register(Context context, String appUsername, PushResponseListener listener, ArrayList<String> tags, boolean forceRegister) {
        register(context, appUsername, listener, tags, forceRegister, new GcmRegistrationService(GoogleCloudMessaging.getInstance(context)), new ServerRegistrationService(context.getString(R.string.server_url_base)));
    }

    protected void register(Context context, String appUsername, PushResponseListener listener, ArrayList<String> tags, boolean forceRegister, GcmRegistrationService gcmService, ServerRegistrationService apiService) {

        setListener(listener);

        Preferences prefs = new Preferences(context);
        registerRx(context, appUsername, tags, forceRegister, gcmService, apiService, prefs)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        serverResult -> {
                            //Si el proceso de registro termina con éxito guardamos el token para no volver a regisrarlo otra vez
                            prefs.setGcmToken(prefs.getTempToken());
                            prefs.setUserPid(serverResult.getData().get(Constants.TASK_PID));
                            notifyListenerSuccess();
                        },
                        error -> {
                            notifyListenerError(Type.CONNECTION_ERROR, error.getMessage());
                            Log.d("Register error: " + error.getMessage());
                        }
                );
    }

    protected Observable<ServerResult> registerRx(Context context, String appUsername, ArrayList<String> tags, boolean forceRegister, GcmRegistrationService gcmService, ServerRegistrationService apiService, Preferences prefs) {
        this.appUsername = appUsername;
        this.tags = tags;
        this.forceRegister = forceRegister;

        // Registro contra Gcm
        return gcmService.register(gcmSenderId)
                .subscribeOn(Schedulers.io())
                .filter(token -> token != null)
                .switchIfEmpty(Observable.error(new IllegalArgumentException("Invalid response from GCM")))
                .doOnNext(token -> prefs.setTempToken(token))
                .filter(token -> forceRegister || !token.equalsIgnoreCase(prefs.getGcmToken()))
                .flatMap(token -> apiService.registerInServer(apiKey, company, appname, token, appUsername, Utils.getApplicationVersion(context), Utils.getUniqueID(context), environment, tags))
                .switchIfEmpty(Observable.just(new ServerResult(true)))
                .filter(serverResult -> serverResult != null)
                .switchIfEmpty(Observable.error(new IOException("Invalid response from server")))
                .flatMap(serverResult -> serverResult.filterErrors());
    }

    public Observable<ServerResult> registerRx(Context context, String appUsername, ArrayList<String> tags, boolean forceRegister) {
        Preferences prefs = new Preferences(context);
        return registerRx(context, appUsername, tags, forceRegister, new GcmRegistrationService(GoogleCloudMessaging.getInstance(context)), new ServerRegistrationService(context.getString(R.string.server_url_base)), prefs);
    }

    public void unregister(Context context, PushResponseListener listener) {
        Preferences prefs = new Preferences(context);

        unregisterRx(new GcmRegistrationService(GoogleCloudMessaging.getInstance(context)), new ServerRegistrationService(context.getString(R.string.server_url_base)), prefs)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        serverResult -> {
                            prefs.clearUserPid();
                            notifyListenerSuccess();
                        },
                        error -> {
                            notifyListenerError(Type.CONNECTION_ERROR, error.getMessage());
                            Log.d("Unregister error: " + error.getMessage());
                        }
                );
    }

    protected Observable<ServerResult> unregisterRx(GcmRegistrationService gcmService, ServerRegistrationService apiService, Preferences prefs) {
        return gcmService.unregister()
                .subscribeOn(Schedulers.io())
                .filter(success -> success)
                .switchIfEmpty(Observable.error(new IllegalArgumentException("Invalid response from GCM")))
                .doOnNext(success -> prefs.clearGcmToken())
                .flatMap(success -> Observable.just(prefs.getUserPid()))
                .filter(userPid -> userPid != null)
                .flatMap(userPid -> apiService.unregisterInServer(apiKey, company, appname, userPid))
                .filter(serverResult -> serverResult != null)
                .defaultIfEmpty(new ServerResult(true));
    }


    public Observable<ServerResult> unregisterRx(Context context, PushResponseListener listener){
        Preferences prefs = new Preferences(context);
        return  unregisterRx(new GcmRegistrationService(GoogleCloudMessaging.getInstance(context)), new ServerRegistrationService(context.getString(R.string.server_url_base)), prefs);
    }

    private void notifyListenerSuccess() {
        if (listener != null) {
            listener.onSuccess();
        }
    }

    private void notifyListenerError(Type errorType, String errorMessage) {
        if (listener != null) {
            listener.onError(new PushError(errorMessage, errorType));
        }
    }

    /**
     * Method to configure the notification behavior
     *
     * @param context          The application context
     * @param sound            Whether enable sound or not, or use the system configuration
     * @param vibration        Whether enable vibration or not, or use the system
     *                         configuration
     * @param autocancel       If the notification is cancel when it is pressed or is
     *                         permanent.
     * @param title            The notification title
     * @param iconResourceName The resource name of the thumbnail to show in a notification
     */
    public void configure(Context context, PushStateType sound, PushStateType vibration, boolean autocancel, String title, String iconResourceName) {

        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREF_PUSH_FILE, Context.MODE_PRIVATE).edit();
        editor.putBoolean(Constants.PREF_AUTOCANCEL, autocancel);
        editor.putString(Constants.PREF_NOTIFICATION_TITLE, title);
        if (iconResourceName != null) {
            editor.putString(Constants.PREF_ICON_RESOURCE, iconResourceName);
        } else {
            editor.remove(Constants.PREF_ICON_RESOURCE);
        }

        if (sound == PushStateType.SYSTEM) {
            editor.remove(Constants.PREF_SOUND);
        } else {
            editor.putBoolean(Constants.PREF_SOUND, sound == PushStateType.ENABLE ? true : false);
        }

        if (vibration == PushStateType.SYSTEM) {
            editor.remove(Constants.PREF_VIBRATION);
        } else {
            editor.putBoolean(Constants.PREF_VIBRATION, vibration == PushStateType.ENABLE ? true : false);
        }
        editor.commit();
    }

    /**
     * Delete the existing configuration and set the default values
     *
     * @param context The application context
     */
    public void setDefaultConfiguration(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.PREF_PUSH_FILE, Context.MODE_PRIVATE).edit();
        editor.remove(Constants.PREF_AUTOCANCEL);
        editor.remove(Constants.PREF_NOTIFICATION_TITLE);
        editor.remove(Constants.PREF_ICON_RESOURCE);
        editor.remove(Constants.PREF_SOUND);
        editor.remove(Constants.PREF_VIBRATION);
        editor.commit();
    }
}