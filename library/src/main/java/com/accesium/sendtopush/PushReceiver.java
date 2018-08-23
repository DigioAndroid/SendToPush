package com.accesium.sendtopush;

import android.content.Context;

import com.accesium.sendtopush.datatypes.PushMessage;
import com.accesium.sendtopush.tools.Log;
import com.accesium.sendtopush.util.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import shortcutbadger.ShortcutBadger;

/**
 * BroadcastReceiver for receive push messages from Google (registration and
 * messages)
 * 
 * @author Isidoro Castell
 *
 */
public class PushReceiver extends FirebaseMessagingService {

	@Override public void onMessageReceived(RemoteMessage message) {
		super.onMessageReceived(message);

		handleMessage(getApplicationContext(), message.getData());
	}

	/**
	 * Process a push message and show it like a notification
	 * 
	 * @param context
	 */
	protected void handleMessage(Context context, Map<String, String> data) {

		try {
			PushMessage pushMessage = PushMessage.buildFromMessage((String) data.values().toArray()[0]);
			if (pushMessage != null) {
				updateBadge(context, pushMessage);
				pushMessage.showNotification(context.getApplicationContext());
			} else {
				Log.d("Error parsing the push message from Google");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updateBadge(Context context, PushMessage pushMessage){
		int badge = Utils.toInteger(pushMessage.getBadge());
		if(badge >= 0) {
			ShortcutBadger.applyCount(context,badge);
		}
	}


}
