package com.reicast.emulator.emu;

import android.util.Log;
import android.view.Surface;

import com.flycast.emulator.NativeGLActivity;

public final class JNIdc {

    private static final String TAG = "JNIdc";

    static {
        try {
            System.loadLibrary("flycast");
            Log.i(TAG, "loaded flycast");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to load libflycast.so", t);
        }
    }

    private JNIdc() {}

    public static native void initEnvironment(Object activity, String filesDir, String homeDir, String locale);

    public static native void setGameUri(String fileName);
    
    public static native void rendinitNative(Surface surface, int width, int height);

    public static native void screenResize(int width, int height);

    public static native void rendtermNative();

    public static native void resumeNative();

    public static native void pauseNative();

    public static native void stopNative();

    public static native void bootGame(String path);

    public static native void rendinitNative(Surface surface, boolean resume);

    public static native void renderNative();

}
