package com.flycast.wrapper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GLView extends GLSurfaceView {

    private final RendererImpl renderer;

    public GLView(Context context) {
        super(context);
        setEGLContextClientVersion(2); // GLES 2.0
        renderer = new RendererImpl();
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        renderer = new RendererImpl();
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }
}
