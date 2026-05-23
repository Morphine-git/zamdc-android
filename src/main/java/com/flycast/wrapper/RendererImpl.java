package com.flycast.wrapper;

import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RendererImpl implements GLSurfaceView.Renderer {

    static {
        try {
            System.loadLibrary("flycast-jni");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // JNI hooks
    private static native void nativeInit();
    private static native void nativeResize(int w, int h);
    private static native void nativeRender();

    private boolean inited = false;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Initialize native GL state once
        nativeInit();
        inited = true;

        // REMOVED Java-side glClearColor so native color is visible
        // If you want Java to control color instead, uncomment below:
        // GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1f); // green
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        if (inited) nativeResize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Let native side render a frame (which clears with its own color)
        nativeRender();
    }
}
