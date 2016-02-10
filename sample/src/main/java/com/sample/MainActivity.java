package com.sample;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.accesium.sendtopush.SendToPushManager;
import com.accesium.sendtopush.datatypes.Environment;
import com.accesium.sendtopush.datatypes.PushError;
import com.accesium.sendtopush.datatypes.PushStateType;
import com.accesium.sendtopush.listeners.PushResponseListener;

public class MainActivity extends AppCompatActivity implements PushResponseListener {

    Button mRegister;
    Button mUnregister;

    //Push
    public final static String APIKEY = "";
    public final static String COMPANY = "";
    public final static String APPNAME = "";
    public final static String GCM_SENDER_ID = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRegister = (Button) findViewById(R.id.register);
        mUnregister = (Button) findViewById(R.id.unregister);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doRegistration();
                Toast.makeText(MainActivity.this, "Registered", Toast.LENGTH_SHORT).show();
            }
        });

        mUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unregister();
                Toast.makeText(MainActivity.this, "Unregistered", Toast.LENGTH_SHORT).show();
            }
        });

        SendToPushManager.init(APIKEY, COMPANY, APPNAME, GCM_SENDER_ID, BuildConfig.DEBUG ? Environment.SANDBOX : Environment.PRODUCTION);


    }

    public void doRegistration() {
        SendToPushManager.getInstance().configure(this, PushStateType.SYSTEM, PushStateType.SYSTEM, true, null, "ic_notification_name");
        SendToPushManager.getInstance().enableDebug(BuildConfig.DEBUG);
        SendToPushManager.getInstance().register(this, "sampleUser", this);

    }

    public void unregister() {
        SendToPushManager.getInstance().unregister(this, this);
    }


    @Override
    public void onSuccess() {
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(PushError error) {
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
    }
}
