package com.fyp.aipoweredcameraapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences prefs;

    private static final String FIRST_LAUNCH = "_.FIRST_LAUNCH";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Preference for first launch
     */
    public void setFirstLaunch(boolean flag) {
        sharedPreferences.edit().putBoolean(FIRST_LAUNCH, flag).apply();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH, true);
    }

    /**
     * To save dialog permission state
     */
    public void setNeverAskAgain(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getNeverAskAgain(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * For other preferences
     * @param key
     * @param value
     */

    public void setIntPref(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getIntPref(String key) { return sharedPreferences.getInt(key, -1); }

    public void setStringPref(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getStringPref(String key) { return sharedPreferences.getString(key, null); }

}
