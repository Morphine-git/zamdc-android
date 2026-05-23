package com.flycast.emulator.emu;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

public class EditVirtualJoystickDelegate implements TouchEventHandler
{
    private final View view;
    private final ScaleGestureDetector scaleGestureDetector;

    private int currentElement = -1;
    private float lastX, lastY;

    // Our VGamepad uses one global layout scale (not per element)
    private float currentScale = 1.0f;

    public EditVirtualJoystickDelegate(View view) {
        this.view = view;
        scaleGestureDetector = new ScaleGestureDetector(view.getContext(), new ScaleGestureListener());
    }

    public void stop() { }

    public void show() {
        VGamepad.show();
        view.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, int width, int height)
    {
        VGamepad.setScreenSize(width, height);

        scaleGestureDetector.onTouchEvent(event);

        if (scaleGestureDetector.isInProgress()) {
            currentElement = -1;
            view.invalidate();
            return true;
        }

        int actionMasked = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        switch (actionMasked)
        {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                currentElement = -1;
                view.invalidate();
                return true;

            case MotionEvent.ACTION_DOWN:
                lastX = event.getX(actionIndex);
                lastY = event.getY(actionIndex);
                currentElement = VGamepad.layoutHitTest(lastX, lastY);
                view.invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (currentElement != -1 && event.getPointerCount() == 1)
                {
                    float x = event.getX(actionIndex);
                    float y = event.getY(actionIndex);
                    VGamepad.translateElement(currentElement, x - lastX, y - lastY);
                    lastX = x;
                    lastY = y;
                    view.invalidate();
                    return true;
                }
                break;
        }

        return false;
    }

    private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector)
        {
            // Must start on a control
            int elemId = VGamepad.layoutHitTest(detector.getFocusX(), detector.getFocusY());
            return elemId != -1;
        }

        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector)
        {
            float factor = detector.getScaleFactor();
            currentScale *= factor;

            // clamp
            if (currentScale < 0.65f) currentScale = 0.65f;
            if (currentScale > 1.45f) currentScale = 1.45f;

            VGamepad.scaleElement(currentElement, currentScale);
            view.invalidate();
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            view.invalidate();
        }
    }
}

