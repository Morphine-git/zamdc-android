package com.flycast.emulator.emu;

import android.view.MotionEvent;

public interface TouchEventHandler {
    boolean onTouchEvent(MotionEvent event, int width, int height);
}