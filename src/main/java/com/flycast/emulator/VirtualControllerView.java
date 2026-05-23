package com.flycast.emulator;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.flycast.emulator.emu.VGamepad;
import android.util.Log;

public class VirtualControllerView extends View {

    public VirtualControllerView(Context context) {
        super(context);
        init();
    }

    public VirtualControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VirtualControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw virtual controller overlay
        VGamepad.setScreenSize(getWidth(), getHeight());
        VGamepad.draw(canvas);

        // Continuous redraw (important for overlays)
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.e("VCV", "TOUCH action=" + event.getActionMasked()
                + " pointers=" + event.getPointerCount());

        VGamepad.setScreenSize(
                getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels
        );

        Log.e("VCV", "FORCED SIZE sent from VirtualControllerView");
        return VGamepad.onTouch(event);
    }

        @Override
        public boolean performClick () {
            return super.performClick();
        }

        // Host lifecycle hooks
        public void onHostResume () {
            try {
                VGamepad.show();
            } catch (Throwable ignored) {
            }
        }

        public void onHostPause () {
            // optional
        }

        public void onHostStop () {
            try {
                VGamepad.hide();
            } catch (Throwable ignored) {
            }
        }
    }
