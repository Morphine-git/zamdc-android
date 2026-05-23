package com.flycast.emulator.periph;

import android.content.Context;
import android.util.Log;

public final class InputDeviceManager {
    private static final String TAG = "IDM";
    private static final String INPUT_TAG = "ZAMDC_INPUT";

    public static final int GAMEPAD_ID = 0x12345678;

    private static final InputDeviceManager INSTANCE = new InputDeviceManager();

    static {
        try {
            System.loadLibrary("zamdc");
            Log.i(TAG, "libzamdc loaded");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to load libzamdc", t);
        }
    }

    private Context appContext;
    private boolean rumbleEnabled = false;
    private boolean hideVirtualControls = false;

    private InputDeviceManager() {}

    public static InputDeviceManager getInstance() {
        return INSTANCE;
    }

    // ---- JNI implemented in android_input.cpp ----
    public native boolean isMicPluggedIn();

    /** Native init() exists in android_input.cpp. */
    public native void init();

    public native void joystickAdded(int id, String name, int maplePort, String uniqueId,
                                     int[] fullAxes, int[] halfAxes, boolean hasRumble);

    public native void joystickRemoved(int id);

    private native void nativeVirtualReleaseAll();
    private native void nativeVirtualJoystick(float x, float y);
    public native void nativeVirtualButtonInput(int controlId, boolean pressed);

    public native boolean joystickButtonEvent(int id, int key, boolean pressed);
    public native boolean joystickAxisEvent(int id, int key, int value);
    public native boolean joystickHatEvent(int id, int key, int value);

    public native boolean keyboardEvent(int key, boolean pressed);
    public native void keyboardText(int c);

    public native boolean mouseEvent(int buttons, int x, int y, int z);
    public native boolean mouseScrollEvent(int x, int y, int z);

    // ---- Java wrappers with logging ----

    public void virtualReleaseAll() {
        Log.e(INPUT_TAG, "JAVA virtualReleaseAll");
        nativeVirtualReleaseAll();
    }

    public void virtualJoystick(float x, float y) {
        Log.e(INPUT_TAG, "JAVA virtualJoystick x=" + x + " y=" + y);
        nativeVirtualJoystick(x, y);
    }

    public void ButtonInput(int controlId, boolean pressed) {
        Log.e(INPUT_TAG, "JAVA virtualButtonInput controlId=" + controlId
                + " pressed=" + (pressed ? 1 : 0));
        nativeVirtualButtonInput(controlId, pressed);
    }

    // ---- Java helpers expected by the rest of the app ----

    public void init(Context ctx) {
        appContext = (ctx != null) ? ctx.getApplicationContext() : null;
        try {
            init();
            Log.i(TAG, "InputDeviceManager.init() OK");
        } catch (Throwable t) {
            Log.e(TAG, "InputDeviceManager.init() FAILED", t);
        }
    }

    public void initOnce(Context ctx) {
        init(ctx);
    }

    public boolean updateRumble() {
        return rumbleEnabled;
    }

    public void stopRumble() {
        rumbleEnabled = false;
    }

    public boolean shouldHideVirtualControls() {
        return hideVirtualControls;
    }

    public void setHideVirtualControls(boolean hide) {
        hideVirtualControls = hide;
    }

    public Context getContext() {
        return appContext;
    }
}
