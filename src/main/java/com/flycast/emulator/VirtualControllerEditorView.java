package com.flycast.emulator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.flycast.emulator.emu.VGamepad;

public class VirtualControllerEditorView extends View {

    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 2.5f;

    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean editMode = true;

    private int activeElemId = -1;

    private float lastX, lastY;

    // pinch
    private boolean pinching = false;
    private float startDist = 0f;
    private float currentScale = 1f;

    public VirtualControllerEditorView(Context c) { super(c); init(); }
    public VirtualControllerEditorView(Context c, AttributeSet a) { super(c, a); init(); }
    public VirtualControllerEditorView(Context c, AttributeSet a, int s) { super(c, a, s); init(); }

    private void init() {
        setClickable(true);
        setFocusable(true);

        // Subtle "gothic glass" overlay so you know you're in edit mode
        overlayPaint.setStyle(Paint.Style.FILL);
        overlayPaint.setAlpha(40);
    }

    public void setEditMode(boolean enabled) {
        editMode = enabled;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (editMode) {
            // Dim overlay only in edit mode
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!editMode) return false;

        final int action = e.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                float x = e.getX();
                float y = e.getY();
                activeElemId = VGamepad.layoutHitTest(x, y); // pick element
                lastX = x;
                lastY = y;
                pinching = false;
                return true;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                if (e.getPointerCount() >= 2 && activeElemId >= 0) {
                    pinching = true;
                    startDist = dist(e);
                }
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                if (activeElemId < 0) return true;

                if (pinching && e.getPointerCount() >= 2) {
                    float d = dist(e);
                    if (startDist > 0f) {
                        float factor = d / startDist;
                        float newScale = clamp(currentScale * factor, MIN_SCALE, MAX_SCALE);
                        VGamepad.scaleElement(activeElemId, newScale);
                    }
                    return true;
                } else {
                    float x = e.getX();
                    float y = e.getY();
                    float dx = x - lastX;
                    float dy = y - lastY;

                    // translate by delta
                    VGamepad.translateElement(activeElemId, dx, dy);

                    lastX = x;
                    lastY = y;
                    return true;
                }
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (pinching) {
                    // lock in scale (best effort)
                    // We don't have a getter for element scale, so we keep our local estimate simple:
                    // assume last scaleElement call was correct.
                }
                pinching = false;
                activeElemId = -1;
                return true;
            }
        }

        return super.onTouchEvent(e);
    }

    private float dist(MotionEvent e) {
        if (e.getPointerCount() < 2) return 0f;
        float x = e.getX(0) - e.getX(1);
        float y = e.getY(0) - e.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}

