package com.accesium.sendtopush.datatypes;

import android.content.Context;
import android.content.SharedPreferences;

import com.accesium.sendtopush.util.Constants;

/**
 * Created by Fran Gilberte on 20/01/2016.
 */
public class Preferences {
    private static final String TEMP_TOKEN_KEY = "temp_token_key";
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEditor;
    private Context mContext;

    public Preferences(Context context) {
        this.mSharedPrefs = context.getSharedPreferences(Constants.PREF_PUSH_FILE, Context.MODE_PRIVATE);
        this.mEditor = mSharedPrefs.edit();
        this.mContext = context;
    }

    public String getGcmToken(){
        return mSharedPrefs.getString(Constants.PREF_TOKEN_KEY, null);
    }

    public void setGcmToken(String token){
        mEditor.putString(Constants.PREF_TOKEN_KEY, token);
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
