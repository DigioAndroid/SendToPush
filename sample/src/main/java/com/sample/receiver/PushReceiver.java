package com.sample.receiver;

import android.content.Context;
import android.content.Intent;

import com.accesium.sendtopush.tools.Log;

/**
 * BroadcastReceiver for receive push messages from Google (registration and
 * messages)
 *
 * @author Isidoro Castell
 */
public class PushReceiver extends com.accesium.sendtopush.PushReceiver {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Broadcast received");
    }

    protected void handleMessage(Context context, Intent intent) {
        Log.d("Extras: " + intent.getExtras().toString());
    }




}
