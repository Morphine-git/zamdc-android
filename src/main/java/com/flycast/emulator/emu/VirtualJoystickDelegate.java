package com.flycast.emulator.emu;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import java.lang.reflect.Method;

import com.flycast.emulator.periph.InputDeviceManager;


/**
 * Routes touch events to VGamepad and controls visibility.
 * Uses InputDeviceManager (fixed) to auto-hide virtual controls
 * when gamepad/mouse is present.
 */
public final class VirtualJoystickDelegate implements TouchEventHandler {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hideRunnable;

    private boolean enabled = true;
    private boolean autoHide = false;
    private int autoHideMs = 2500;

    public VirtualJoystickDelegate(Context ctx) {
        // init both systems
        VGamepad.init(ctx);
        InputDeviceManager.getInstance().init(ctx);

        hideRunnable = new Runnable() {
            @Override public void run() {
                if (autoHide) VGamepad.hide();
            }
        };
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) VGamepad.hide();
    }

    public void setAutoHide(boolean autoHide, int ms) {
        this.autoHide = autoHide;
        this.autoHideMs = Math.max(250, ms);
    }

    public void show() {
        if (!enabled) return;
        VGamepad.show();
        scheduleHide();
    }

    public void hide() {
        VGamepad.hide();
        handler.removeCallbacks(hideRunnable);
    }

    private void scheduleHide() {
        handler.removeCallbacks(hideRunnable);
        if (autoHide) handler.postDelayed(hideRunnable, autoHideMs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, int width, int height) {
        if (!enabled) return false;

        // update layout
        VGamepad.setScreenSize(width, height);

        // If Flycast GUI is open, DO NOT steal touch from it.
        if (isGuiOpenSafe()) {
            return false;
        }

        // ✅ Hide virtual controls if a controller/mouse is connected
        try {
            if (InputDeviceManager.getInstance().shouldHideVirtualControls()) {
                VGamepad.hide();
                return false;
            }
        } catch (Throwable ignored) {}

        // Always show when user touches
        if (event != null) {
            int a = event.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_POINTER_DOWN) {
                VGamepad.show();
                scheduleHide();
            }
        }

        return VGamepad.onTouch(event);
    }

    private boolean isGuiOpenSafe() {
        // Try JNIdc.guiIsOpen() in whichever package exists.
        return callStaticBool("com.reicast.emulator.emu.JNIdc", "guiIsOpen")
                || callStaticBool("com.flycast.emulator.emu.JNIdc", "guiIsOpen")
                || callStaticBool("com.flycast.wrapper.JNIdc", "guiIsOpen")
                || callStaticBool("com.reicast.emulator.emu.JNIdc", "isGuiOpen");
    }

    private boolean callStaticBool(String className, String methodName) {
        try {
            Class<?> c = Class.forName(className);
            Method m = c.getDeclaredMethod(methodName);
            m.setAccessible(true);
            Object r = m.invoke(null);
            if (r instanceof Boolean) return (Boolean) r;
        } catch (Throwable ignored) {}
        return false;
    }
}

