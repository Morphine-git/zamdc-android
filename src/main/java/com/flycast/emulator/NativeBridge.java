package com.flycast.emulator;

import android.util.Log;

public final class NativeBridge {
    private static final String TAG = "NativeBridge";
    private static boolean loaded = false;

    private NativeBridge() {}

    public static boolean load() {
        if (loaded) return true;
        try {
            System.loadLibrary("flycast");   // libflycast.so
            loaded = true;
            Log.i(TAG, "Loaded libflycast");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to load libflycast", t);
            loaded = false;
        }
        return loaded;
    }

    // Example optional call — you wrap EVERY native method like this
    private static native void reloadConfigNative();

    public static void reloadConfigSafe() {
        if (!load()) return;
        try {
            reloadConfigNative();
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "reloadConfig not linked in this build", e);
        }
    }
}