package com.accesium.sendtopush.util;

/**
 * Constants of the library
 * 
 * @author Isidoro Castell
 * 
 */
public class Constants {
	// GCM actions
	public final static String GCM_REGISTER_ACTION = "com.google.android.c2dm.intent.REGISTER";
	public final static String GCM_REGISTRATION_ACTION = "com.google.android.c2dm.intent.REGISTRATION";
	public final static String GCM_RECEIVE_ACTION = "com.google.android.c2dm.intent.RECEIVE";
	public final static String GCM_UNREGISTER_ACTION = "com.google.android.c2dm.intent.UNREGISTER";
	public static final String GCM_MESSAGE_EXTRA = "message";
	// GCM registration values
	public final static String REGISTRATION_ID = "registration_id";
	public final static String ERROR = "error";
	public final static String UNREGISTERED = "unregistered";
	// Preferences keys
	public static final String PREF_PUSH_FILE = "sendToPushPref";
	public static final String PREF_TOKEN_KEY = "pref_token_key";
	// Service keys
	public static final String SERVICE_TOKEN_KEY = "service_token_key";
	public static final String SERVICE_TASK_KEY = "service_task_key";
	public static final String SERVICE_USERNAME_KEY = "service_username_key";
	public static final String SERVICE_RESULT_PUSH_REQUEST = "service_result_push_request";
	public static final String PUSH_REQUEST_SUCCESS = "push_request_success";
	public static final String PUSH_REQUEST_ERROR = "push_request_error";
	// Task keys
	public static final String TASK = "task";
	public static final String TASK_REGISTER = "register";
	public static final String TASK_UNREGISTER_ID = "unregisterid";
	public static final String TASK_APIKEY = "apikey";
	public static final String TASK_COMPANY = "company";
	public static final String TASK_APPNAME = "appname";
	public static final String TASK_DEVICE_TOKEN = "devicetoken";
	public static final String TASK_APP_VERSION = "appversion";
	public static final String TASK_DEVICE_UID = "deviceuid";
	public static final String TASK_DEVICE_NAME = "devicename";
	public static final String TASK_DEVICE_MODEL = "devicemodel";
	public static final String TASK_DEVICE_VERSION = "deviceversion";
	public static final String TASK_APP_USERNAME = "appusername";
	public static final String TASK_PUSH_BADGE = "pushbadge";
	public static final String TASK_PUSH_ALERT = "pushalert";
	public static final String TASK_PUSH_SOUND = "pushsound";
	public static final String TASK_TAGS = "tags";
	public static final String TASK_PID = "pid";
	public static final String TASK_ENVIRONMENT = "environment";
	// Intent
	public static final String PUSH_MESSAGE = "push_message";
	public static final String PUSH_MESSAGE_OBJECT = "push_message_object";

	// PushStateType sound, PushStateType vibration, boolean autocancel, String
	// title, int iconResource
	public static final String PREF_SOUND = "pref_sound";
	public static final String PREF_VIBRATION = "pref_vibration";
	public static final String PREF_AUTOCANCEL = "pref_autocancel";
	public static final String PREF_NOTIFICATION_TITLE = "pref_notification_title";
	public static final String PREF_ICON_RESOURCE = "pref_icon_resource";
}
