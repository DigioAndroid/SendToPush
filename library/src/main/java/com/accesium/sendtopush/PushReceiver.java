package com.accesium.sendtopush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.accesium.sendtopush.datatypes.PushMessage;
import com.accesium.sendtopush.tools.Log;
import com.accesium.sendtopush.util.Constants;

/**
 * BroadcastReceiver for receive push messages from Google (registration and
 * messages)
 * 
 * @author Isidoro Castell
 * 
 */
public class PushReceiver extends BroadcastReceiver {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Broadcast received");
		if (intent.getAction().equals(Constants.GCM_RECEIVE_ACTION)) {
			handleMessage(context, intent);
		}
	}

	/**
	 * Process a push message and show it like a notification
	 * 
	 * @param context
	 * @param intent
	 */
	protected void handleMessage(Context context, Intent intent) {
		Log.d("Extras: " + intent.getExtras().toString());

		String message = intent.getExtras().getString(
				Constants.GCM_MESSAGE_EXTRA);
		PushMessage pushMessage = PushMessage.buildFromMessage(message);
		if (pushMessage != null) {
			pushMessage.showNotification(context.getApplicationContext());
		} else {
			Log.d("Error parsing the push message from Google");
		}
	}


}
