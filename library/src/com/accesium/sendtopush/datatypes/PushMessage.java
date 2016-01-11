package com.accesium.sendtopush.datatypes;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;

import com.accesium.sendtopush.SendToPushManager;
import com.accesium.sendtopush.tools.Log;
import com.accesium.sendtopush.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class that encapsulates the JSON response of a message from GCM
 * 
 * @author Isidoro Castell
 * 
 */
public class PushMessage implements Parcelable {

	private String alert;
	private String badge;
	private String sound;
	private String message;
	private String custom;

    private static int notif_ref = 101;

	public PushMessage(String alert, String badge, String sound, String message) {
		super();
		this.alert = alert;
		this.badge = badge;
		this.sound = sound;
		this.message = message;
	}

	/**
	 * Build a PushMessage object from a GCM message received in a push message
	 * 
	 * @param message
	 * @return
	 */
	public static PushMessage buildFromMessage(String message) {
		if(message!=null){
			Log.d(message);
		}
		try {
			JSONObject messageJson = new JSONObject(message);
			String apsString = messageJson.getString("aps");
			Gson gson = new GsonBuilder().create();
			PushMessage result = gson.fromJson(apsString, PushMessage.class);
			result.setCustom(message);
			return result;
		} catch (Exception e) {
			Log.e("", e);
		}
		return null;
	}

	/**
	 * @return the alert
	 */
	public String getAlert() {
		return alert;
	}

	/**
	 * @param alert
	 *            the alert to set
	 */
	public void setAlert(String alert) {
		this.alert = alert;
	}

	/**
	 * @return the badge
	 */
	public String getBadge() {
		return badge;
	}

	/**
	 * @param badge
	 *            the badge to set
	 */
	public void setBadge(String badge) {
		this.badge = badge;
	}

	/**
	 * @return the sound
	 */
	public String getSound() {
		return sound;
	}

	/**
	 * @param sound
	 *            the sound to set
	 */
	public void setSound(String sound) {
		this.sound = sound;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the custom
	 */
	public String getCustom() {
		return custom;
	}

	/**
	 * @param custom
	 *            the custom to set
	 */
	public void setCustom(String custom) {
		this.custom = custom;
	}

	/**
	 * Show a message in the notification center bar. It looks for the
	 * preferences set in
	 * {@link SendToPushManager#configure(Context,PushStateType,PushStateType,boolean,String,int)
	 * SendToPushManager#configure}
	 * 
	 * @param context
	 */
	public void showNotification(Context context) {
		showNotification(context, null);
	}

    /**
     * Shows notification with a specified notificaton Id
     * @param context
     * @param notifId
     */
    public void showNotification(Context context, int notifId) {
        showNotification(context, null, notifId);
    }


    /**
     * Shows notification with notif_ref id
     * @param context
     * @param intentMessage
     */
    public void showNotification(Context context, String intentMessage) {
        notif_ref++;
        showNotification(context, intentMessage, notif_ref);
    }

	public void showNotification(Context context, String intentMessage, int notifId) {
		// Build the intent which will be launch when press on the notification
		// Get the default launch activity for the current application
		final String packageName = context.getApplicationContext().getPackageName();
		PackageManager pm = context.getPackageManager();
		Intent pending = pm.getLaunchIntentForPackage(packageName);
		if (intentMessage != null) {
			pending.putExtra(Constants.PUSH_MESSAGE, intentMessage);
		} else {
			pending.putExtra(Constants.PUSH_MESSAGE, (alert != null) ? alert : message);
		}
		pending.putExtra(Constants.PUSH_MESSAGE_OBJECT, this);
		pending.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		// If there are several messages at the same time or in a short amount
		// of time, to enable them
		pending.setAction("actionstring" + System.currentTimeMillis());
		// Get the values set in configuration method
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_PUSH_FILE, Context.MODE_PRIVATE);

		String title = prefs.getString(Constants.PREF_NOTIFICATION_TITLE, getApplicationName(context));

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, pending, PendingIntent.FLAG_CANCEL_CURRENT);

		final boolean autoCancel = prefs.getBoolean(Constants.PREF_AUTOCANCEL, true);
		// Build the notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setWhen(System.currentTimeMillis()).setContentTitle(title).setContentText((alert != null) ? alert : message)
				.setContentIntent(contentIntent).setAutoCancel(autoCancel);

		NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
		style.bigText((alert != null) ? alert : message);
		builder.setStyle(style);
		
		// Get the status icon identifier
		final String iconRes = prefs.getString(Constants.PREF_ICON_RESOURCE, "ic_stat_push_msg");
		final int iconId = context.getResources().getIdentifier(iconRes, "drawable", context.getPackageName());
		// Set icon
		builder.setSmallIcon(iconId);

		Notification notification = builder.build();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		int sound = Notification.DEFAULT_SOUND;
		if (prefs.contains(Constants.PREF_SOUND)) {
			sound = prefs.getBoolean(Constants.PREF_SOUND, false) ? Notification.DEFAULT_SOUND : 0;
		}

		int vibration = Notification.DEFAULT_VIBRATE;
		if (prefs.contains(Constants.PREF_VIBRATION)) {
			vibration = prefs.getBoolean(Constants.PREF_VIBRATION, false) ? Notification.DEFAULT_VIBRATE : 0;
		}

		notification.defaults = notification.defaults | sound | vibration;
		notificationManager.notify(notifId, notification);
	}

	protected String getApplicationName(Context context) {
		final PackageManager pm = context.getApplicationContext().getPackageManager();
		ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo(context.getPackageName(), 0);
		} catch (final NameNotFoundException e) {
			ai = null;
		}
		final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(PushNotificationCenter)");

		return applicationName;
	}
	
	protected PushMessage(Parcel in) {
        alert = in.readString();
        badge = in.readString();
        sound = in.readString();
        message = in.readString();
        custom = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(alert);
        dest.writeString(badge);
        dest.writeString(sound);
        dest.writeString(message);
        dest.writeString(custom);
    }

    public static final Parcelable.Creator<PushMessage> CREATOR = new Parcelable.Creator<PushMessage>() {
        @Override
        public PushMessage createFromParcel(Parcel in) {
            return new PushMessage(in);
        }

        @Override
        public PushMessage[] newArray(int size) {
            return new PushMessage[size];
        }
    };


}
