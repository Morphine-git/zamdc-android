package com.flycast.emulator;

public class Core {

    static {
        // Your APK has libflycast.so, not libflycast-jni.so
        System.loadLibrary("zamdc");
    }

    // Native API – these must match the functions exported by libflycast.so
    public static native void init();
    public static native void loadBios(String path);
    public static native void loadGame(String path);
    public static native void runFrame();
    public static native void onInput(int code, boolean pressed);
    public static native void saveState(String path);
    public static native void loadState(String path);
    public static native void shutdown();
}

