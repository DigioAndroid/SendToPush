package com.accesium.sendtopush.datatypes;

import android.content.Context;
import android.content.SharedPreferences;

import com.accesium.sendtopush.util.Constants;

/**
 * Created by Fran Gilberte on 20/01/2016.
 */
public class Preferences {
    public static final String PREF_PUSH_FILE = "sendToPushPref";
    private static final String TEMP_TOKEN_KEY = "temp_token_key";
    public static final String TASK_PID = "pid";


    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEditor;
    private Context mContext;

    public Preferences(Context context) {
        this.mSharedPrefs = context.getSharedPreferences(PREF_PUSH_FILE, Context.MODE_PRIVATE);
        this.mEditor = mSharedPrefs.edit();
        this.mContext = context;
    }

    public String getUserPid(){
        return mSharedPrefs.getString(TASK_PID, null);
    }

    public void setUserPid(String pid){
        mEditor.putString(TASK_PID, pid);
        mEditor.commit();
    }

    public void clearUserPid(){
        mEditor.remove(Constants.TASK_PID);
        mEditor.commit();
    }

    public String getGcmToken(){
        return mSharedPrefs.getString(Constants.PREF_TOKEN_KEY, null);
    }

    public void setGcmToken(String token){
        mEditor.putString(Constants.PREF_TOKEN_KEY, token);
        mEditor.commit();
    }

    public void clearGcmToken(){
        mEditor.remove(Constants.PREF_TOKEN_KEY);
        mEditor.commit();
    }

    public String getTempToken(){
        return mSharedPrefs.getString(TEMP_TOKEN_KEY, null);
    }

    public void setTempToken(String token){
        mEditor.putString(TEMP_TOKEN_KEY, token);
        mEditor.commit();
    }

}
