package com.flycast.emulator;

import android.app.Application;
import android.util.Log;

public class ZamdcApplication extends Application {

    private static final String TAG = "ZamdcApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "App started");
        // 🚫 Do NOT call any native methods here.
        // Native init happens after GL surface exists (in NativeGLView).
    }

    // Native may call these on the Application; keep Java-only stubs.
    public void SaveAndroidSettings(String path) {
        try {
            getSharedPreferences("flycast_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("android_settings_path", path)
                    .apply();
        } catch (Throwable t) {
            Log.e(TAG, "SaveAndroidSettings failed", t);
        }
    }

    public String LoadAndroidSettings() {
        try {
            return getSharedPreferences("flycast_prefs", MODE_PRIVATE)
                    .getString("android_settings_path", "");
        } catch (Throwable t) {
            Log.e(TAG, "LoadAndroidSettings failed", t);
            return "";
        }
    }
}

