package com.flycast.emulator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class AppGlobals {

    private static Activity currentActivity;

    private AppGlobals() {}

    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static SharedPreferences getConfigurationPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }
}

