package com.accesium.sendtopush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.accesium.sendtopush.datatypes.Environment;
import com.accesium.sendtopush.datatypes.PushError;
import com.accesium.sendtopush.datatypes.PushError.Type;
import com.accesium.sendtopush.datatypes.PushStateType;
import com.accesium.sendtopush.datatypes.ServerTask;
import com.accesium.sendtopush.listeners.PushResponseListener;
import com.accesium.sendtopush.service.PushRegisterService;
import com.accesium.sendtopush.tools.Log;
import com.accesium.sendtopush.util.Constants;
import com.accesium.sendtopush.util.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that encapsulate the register and unregister in push server. It handles
 * the registration in Google's server, querying a push token. After this, it is
 * registered within our push server by sending the required parameters.
 * 
 * @author Isidoro Castell
 * 
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
	 * @param apiKey
	 *            The generated apikey in the push server
	 * @param company
	 *            Your company ID
	 * @param appname
	 *            APP ID in the push server
	 * @param gcmSenderId
	 *            The GCM Sender ID
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
	 * @throws IllegalStateException
	 *             If the object has not been initialized
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
	 * @param apiKey
	 *            Generated in the push server, when the project has been
	 *            register
	 * @param company
	 *            Company registered in the push server
	 * @param appname
	 *            Name of the application in the push server
	 * @param gcmSenderId
	 *            The GCM Sender ID obtained from Google APIs Console page
	 * @param environment
	 *            The environment which the application is executed
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

	public static boolean isInitialized(){
		return sInstance!=null;
	}
	
	/**
	 * Enable or disable the logging in the library
	 * 
	 * @param enable
	 *            Whether enable or not
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
	 * @param appUsername
	 *            the appUsername to set
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
	 * @param tags
	 *            the tags to set
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
	 * @param listener
	 *            the listener to set
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
	 * @param context
	 *            The application context
	 * @param appUsername
	 *            The user name to save in our push server. This will be used to
	 *            send push messages.
	 * @param listener
	 *            The callback to receive if the query was successful or not.
	 */
	public void register(Context context, String appUsername, PushResponseListener listener) {
		register(context, appUsername, listener, null);
	}

	/**
	 * Start the registration process. First it queries the GCM token, once this
	 * is done it is registered in our push server.
	 * 
	 * @param context
	 *            The application context
	 * @param appUsername
	 *            The user name to save in our push server. This will be used to
	 *            send push messages.
	 * @param listener
	 *            The callback to receive if the query was successful or not.
	 * @param tags
	 *            Tags list used to associate a device with a specific group
	 */
	public void register(Context context, String appUsername, PushResponseListener listener, ArrayList<String> tags) {
		register(context, appUsername, listener, tags, false);
	}
	
	public void register(Context context, String appUsername, PushResponseListener listener, ArrayList<String> tags, boolean forceRegister) {
		this.listener = listener;
		this.appUsername = appUsername;
		this.tags = tags;
		this.forceRegister = forceRegister;
		// Query to google the device token
		registerInBackground(context);
	}

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground(final Context context) {
        AsyncTask registerTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {

                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

                    String regid = gcm.register(gcmSenderId);

                    handleRegistration(context, regid);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    // Registration failed, should try again later.
                    Log.d("Registration failed, error = " + ex.getMessage());
                    // Build an error object
                    PushError pushError = new PushError(ex.getMessage(), Type.GET_PUSH_TOKEN_ERROR);
                    // Notify to the listener
                    if (listener != null) {
                        listener.onError(pushError);
                    }
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                super.onPostExecute(msg);
            }

        };

        Utils.executeAsyncTask(registerTask);

    }

	/**
	 * Handles a registration message from GCM and registers in our server with
	 * the token received. When the process is done it notifies to the
	 * {@link PushResponseListener IPushResponseListener} given.
	 * 
	 * @param context
	 *            The application context
	 */
	protected void handleRegistration(Context context, String token) {


		if (token != null) {
			Log.d("Token received -> token:" + token);
			
			SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_PUSH_FILE, Context.MODE_PRIVATE);
			// If the token is different
			if (!((prefs.getString(Constants.PREF_PUSH_FILE, "").compareTo(token)) == 0) || forceRegister) {
				Log.d("Launch the service to send the token to the server");
				// Launch the service which send the token to the server
				Intent serviceIntent = new Intent(context, PushRegisterService.class);
				serviceIntent.putExtra(Constants.SERVICE_TOKEN_KEY, token);
				serviceIntent.putExtra(Constants.SERVICE_TASK_KEY, (Parcelable) ServerTask.REGISTER);
				serviceIntent.putExtra(Constants.TASK_APIKEY, apiKey);
				serviceIntent.putExtra(Constants.TASK_COMPANY, company);
				serviceIntent.putExtra(Constants.TASK_APPNAME, appname);
				serviceIntent.putExtra(Constants.TASK_APP_USERNAME, appUsername);
				serviceIntent.putExtra(Constants.TASK_ENVIRONMENT, environment.getEnvironment());
				if(tags!=null){
					serviceIntent.putStringArrayListExtra(Constants.TASK_TAGS, tags);
				}
				context.startService(serviceIntent);
				// Register for the service response
				LocalBroadcastManager.getInstance(context)
						.registerReceiver(mServiceReceiver, new IntentFilter(Constants.SERVICE_RESULT_PUSH_REQUEST));
			}
		}
	}

	/**
	 * Unregister from the GCM server and our push server
	 * 
	 * @param context
	 *            The application context
	 * @param listener
	 *            The callback for the status of the process.
	 */
	public void unregister(Context context, PushResponseListener listener) {

		// Unregister from GCM
        try {
            GoogleCloudMessaging.getInstance(context).unregister();
        } catch(IOException ex){
            Log.d("Unregister error: " + ex.getMessage());
        }

        // unregistration done, new messages from the authorized sender will
        // be rejected
        Log.d("unregistered");
        // Notify
        if (listener != null) {
            listener.onSuccess();
        }

        // Delete the token from preferences
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_PUSH_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.PREF_TOKEN_KEY);
        editor.commit();

		// Get the user pid returned by the server in the registration process
		final String userPid = prefs.getString(Constants.TASK_PID, null);
		if (userPid != null) {
			// Unregister from push server
			Intent serviceIntent = new Intent(context, PushRegisterService.class);
			serviceIntent.putExtra(Constants.SERVICE_TASK_KEY, (Parcelable) ServerTask.UNREGISTER);
			serviceIntent.putExtra(Constants.TASK_APIKEY, apiKey);
			serviceIntent.putExtra(Constants.TASK_COMPANY, company);
			serviceIntent.putExtra(Constants.TASK_APPNAME, appname);
			serviceIntent.putExtra(Constants.TASK_PID, userPid);
			context.startService(serviceIntent);
			// Register for the service response
			LocalBroadcastManager.getInstance(context).registerReceiver(mServiceReceiver, new IntentFilter(Constants.SERVICE_RESULT_PUSH_REQUEST));
		} else {
			if (listener != null) {
				PushError error = new PushError("No pid found", Type.INCORRECT_PARAM);
				listener.onError(error);
			}
		}
	}

	/**
	 * Method to configure the notification behavior
	 * 
	 * @param context
	 *            The application context
	 * @param sound
	 *            Whether enable sound or not, or use the system configuration
	 * @param vibration
	 *            Whether enable vibration or not, or use the system
	 *            configuration
	 * @param autocancel
	 *            If the notification is cancel when it is pressed or is
	 *            permanent.
	 * @param title
	 *            The notification title
	 * @param iconResourceName
	 *            The resource name of the thumbnail to show in a notification
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
	 * @param context
	 *            The application context
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

	private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (listener != null) {
				final boolean success = intent.getBooleanExtra(Constants.PUSH_REQUEST_SUCCESS, false);
				if (success) {
					listener.onSuccess();
				} else {
					final PushError error = intent.getParcelableExtra(Constants.PUSH_REQUEST_ERROR);
					listener.onError(error);
				}
			}
			// Unregister
			LocalBroadcastManager.getInstance(context).unregisterReceiver(mServiceReceiver);
		};
	};
}
