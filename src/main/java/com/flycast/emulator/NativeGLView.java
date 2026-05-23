package com.flycast.emulator;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.Surface;

import com.reicast.emulator.emu.ZamJNIdc;
import com.reicast.emulator.emu.JNIdc;
import com.flycast.emulator.emu.VGamepad;

public class NativeGLView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "NativeGLView";

    private Surface surface;
    private boolean surfaceReady = false;

    public NativeGLView(Context context) {
        super(context);
        init();
    }

    public NativeGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NativeGLView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);

        // Keep it simple and visible
        setOpaque(true);
        setAlpha(1.0f);
        setVisibility(VISIBLE);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        Log.i(TAG, "init()");
    }

    // =============================
    // Surface callbacks
    // =============================

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        Log.i(TAG, "Surface available: " + width + "x" + height);

        surface = new Surface(texture);
        surfaceReady = true;

        try {
            // This is the critical call into native
            JNIdc.rendinitNative(surface, width, height);
        } catch (Throwable t) {
            Log.e(TAG, "rendinitNative failed", t);
        }

         try {
             // JNIdc.screenResize(width, height);
       } catch (Throwable t) {
             Log.e(TAG, "screenResize failed", t);
         }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        Log.i(TAG, "Surface size changed: " + width + "x" + height);

        try {
            // JNIdc.screenResize(width, height);
        } catch (Throwable t) {
            Log.e(TAG, "screenResize failed", t);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        Log.i(TAG, "Surface destroyed");

        surfaceReady = false;

        try {
           JNIdc.rendinitNative(null, 0,0);
        } catch (Throwable t) {
            Log.e(TAG, "rendtermNative stop failed", t);
        }

        if (surface != null) {
            surface.release();
            surface = null;
        }

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        // no-op
    }

    // =============================
    // Activity lifecycle hooks
    // =============================

    public void onHostResume() {
        Log.i(TAG, "onHostResume()");

        try {
           // JNIdc.resumeNative();
        } catch (Throwable t) {
            Log.e(TAG, "resumeNative failed", t);
        }
    }

    public void onHostPause() {
        Log.i(TAG, "onHostPause()");

        try {
            // JNIdc.pauseNative();
        } catch (Throwable t) {
            Log.e(TAG, "pauseNative failed", t);
        }
    }

    public void onHostStop() {
        Log.i(TAG, "onHostStop()");
    }

    // =============================
    // Touch (disabled for now)
    // =============================

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.e("VCV", "TOUCH action=" + event.getActionMasked()
                + " pointers=" + event.getPointerCount());

        VGamepad.setScreenSize(
                getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels
        );

        Log.e("VCV", "FORCED SIZE sent to VGamepad");

        Log.e("VCV", "WIDTH="
                + getResources().getDisplayMetrics().widthPixels
                + " HEIGHT="
                + getResources().getDisplayMetrics().heightPixels);

        if (VGamepad.onTouch(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }
}
