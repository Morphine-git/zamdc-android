package com.reicast.emulator.emu;

import android.app.Activity;
import android.util.Log;
import android.view.Surface;

public final class ZamJNIdc {

    private static final String TAG = "ZamJNIdc";

    static {
        try {
            System.loadLibrary("flycast");
            Log.i(TAG, "Loaded flycast");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to load flycast", t);
        }

        try {
            System.loadLibrary("zamdc");
            Log.i(TAG, "Loaded zamdc");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to load zamdc", t);
        }
    }

    private ZamJNIdc() {
    }

    // Native methods


}
