package com.sample.receiver;

import com.accesium.sendtopush.PushReceiver;
import com.accesium.sendtopush.tools.Log;
import com.google.firebase.messaging.RemoteMessage;

/**
 * BroadcastReceiver for receive push messages from Google (registration and
 * messages)
 *
 * @author Isidoro Castell
 */
public class MyPushReceiver extends PushReceiver {

    @Override public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("Message proccesed");
    }
}
