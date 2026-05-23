package com.flycast.emulator.emu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.flycast.emulator.periph.InputDeviceManager;
import com.flycast.wrapper.R;

public final class VGamepad {
    private static final String TAG = "VGamepad";

    private static Bitmap buttonsTexture;

    private static final Rect SRC_A = new Rect(0, 0, 64, 64);
    private static final Rect SRC_B = new Rect(64, 0, 128, 64);
    private static final Rect SRC_X = new Rect(128, 0, 192, 64);
    private static final Rect SRC_Y = new Rect(192, 0, 256, 64);

    private static final Rect SRC_START = new Rect(0, 64, 128, 128);
    private static final Rect SRC_ANALOG_RING = new Rect(0, 128, 128, 256);
    private static final Rect SRC_ANALOG_STICK = new Rect(128, 128, 192, 192);
    private static final Rect SRC_DPAD = new Rect(256, 0, 384, 128);
    private static final Rect SRC_L = new Rect(0, 256, 128, 320);
    private static final Rect SRC_R = new Rect(128, 256, 256, 320);

    public static final int BTN_LEFT = 0;
    public static final int BTN_RIGHT = 2;
    public static final int BTN_UP = 1;
    public static final int BTN_DOWN = 3;
    public static final int BTN_START = 8;

    public static final int BTN_A = 7;
    public static final int BTN_B = 6;
    public static final int BTN_X = 4;
    public static final int BTN_Y = 5;

    public static final int BTN_L = 9;
    public static final int BTN_R = 10;

    public static final int ANALOG = 11;

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    private static boolean visible = true;
    private static int activeControlId = -1;
    private static boolean pressed = false;

    private static float analogX = 0f;
    private static float analogY = 0f;

    private static boolean analogActive = false;

    private static final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final RectF leftRect = new RectF();
    private static final RectF rightRect = new RectF();
    private static final RectF upRect = new RectF();
    private static final RectF downRect = new RectF();

    private static final RectF startRect = new RectF();
    private static final RectF lRect = new RectF();
    private static final RectF rRect = new RectF();

    private static float analogAreaX;
    private static float analogAreaY;
    private static float analogAreaSize;
    private static float analogStickSize;

    private static float ax, ay, bx, by, xx, xy, yx, yy;
    private static float faceButtonSize;

    private VGamepad() {}

    public static void init(Context ctx) {
        try {
            if (buttonsTexture == null) {
                buttonsTexture = BitmapFactory.decodeResource(
                        ctx.getResources(),
                        R.drawable.buttons
                );
            }

            if (buttonsTexture != null) {
                Log.i(TAG, "buttons.png loaded");
            } else {
                Log.e(TAG, "buttons.png failed to load");
            }
        } catch (Throwable t) {
            Log.e(TAG, "buttons.png load error", t);
        }

        Log.i(TAG, "VGamepad init");
    }

    public static void setScreenSize(int width, int height) {
        if (width > 0 && height > 0) {
            if (screenWidth != width || screenHeight != height) {
                Log.i(TAG, "setScreenSize width=" + width + " height=" + height);
            }

            screenWidth = width;
            screenHeight = height;
            updateLayout();
        }
    }

    public static void show() {
        visible = true;
    }

    public static void hide() {
        visible = false;
    }

    private static void updateLayout() {
        float dcw = screenWidth;
        float dch = screenHeight;
        float uiscale = screenWidth / 2101f;

        // A/B/X/Y diamond
        float buttonsX = dcw * 0.72f;
        float buttonsY = dch * 0.56f;
        float scale = 1.7f * uiscale;
        faceButtonSize = 64f * scale;

        xx = buttonsX + 0f * faceButtonSize;
        xy = buttonsY + 1f * faceButtonSize;

        yx = buttonsX + 1f * faceButtonSize;
        yy = buttonsY + 0f * faceButtonSize;

        bx = buttonsX + 2f * faceButtonSize;
        by = buttonsY + 1f * faceButtonSize;

        ax = buttonsX + 1f * faceButtonSize;
        ay = buttonsY + 2f * faceButtonSize;

        // Start
        float startW = dcw * 0.14f;
        float startH = dch * 0.10f;
        float startX = dcw * 0.45f;
        float startY = dch * 0.76f;
        startRect.set(startX, startY, startX + startW, startY + startH);

        // Triggers
        lRect.set(dcw * 0.03f, dch * 0.03f, dcw * 0.18f, dch * 0.11f);
        rRect.set(dcw * 0.82f, dch * 0.03f, dcw * 0.97f, dch * 0.11f);

        // Analog
        analogAreaX = dcw * 0.03f;
        analogAreaY = dch * 0.13f;
        analogAreaSize = dcw * 0.23f;
        analogStickSize = dcw * 0.075f;

        // Smaller D-pad, like A/B/X/Y size
        float dpadCell = faceButtonSize * 0.90f;
        float dpadGap = faceButtonSize * 0.18f;

        float dpadCenterX = dcw * 0.115f;
        float dpadCenterY = dch * 0.735f;

        float step = dpadCell + dpadGap;

        upRect.set(
                dpadCenterX - dpadCell * 0.5f,
                dpadCenterY - step - dpadCell * 0.5f,
                dpadCenterX + dpadCell * 0.5f,
                dpadCenterY - step + dpadCell * 0.5f
        );

        downRect.set(
                dpadCenterX - dpadCell * 0.5f,
                dpadCenterY + step - dpadCell * 0.5f,
                dpadCenterX + dpadCell * 0.5f,
                dpadCenterY + step + dpadCell * 0.5f
        );

        leftRect.set(
                dpadCenterX - step - dpadCell * 0.5f,
                dpadCenterY - dpadCell * 0.5f,
                dpadCenterX - step + dpadCell * 0.5f,
                dpadCenterY + dpadCell * 0.5f
        );

        rightRect.set(
                dpadCenterX + step - dpadCell * 0.5f,
                dpadCenterY - dpadCell * 0.5f,
                dpadCenterX + step + dpadCell * 0.5f,
                dpadCenterY + dpadCell * 0.5f
        );
    }

    public static int layoutHitTest(float x, float y) {
        if (screenWidth <= 0 || screenHeight <= 0) return -1;

        if (lRect.contains(x, y)) return BTN_L;
        if (rRect.contains(x, y)) return BTN_R;

        if (inAnalogArea(x, y)) return ANALOG;

        if (leftRect.contains(x, y)) return BTN_LEFT;
        if (rightRect.contains(x, y)) return BTN_RIGHT;
        if (upRect.contains(x, y)) return BTN_UP;
        if (downRect.contains(x, y)) return BTN_DOWN;

        if (startRect.contains(x, y)) return BTN_START;

        if (inButtonCircle(x, y, ax, ay)) return BTN_A;
        if (inButtonCircle(x, y, bx, by)) return BTN_B;
        if (inButtonCircle(x, y, xx, xy)) return BTN_X;
        if (inButtonCircle(x, y, yx, yy)) return BTN_Y;

        return -1;
    }

    public static boolean onTouch(MotionEvent event) {
        if (event == null) return false;

        float x = event.getX();
        float y = event.getY();

        ensureVirtualGamepadRegistered();

        int hit = layoutHitTest(x, y);
        int action = event.getActionMasked();

        Log.e(TAG, "HIT TEST x=" + x + " y=" + y + " hit=" + hit);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (hit >= 0) {
                    activeControlId = hit;
                    pressed = true;

                    if (hit == ANALOG) {
                        analogActive = true;
                        updateAnalog(x, y);
                    } else {
                        Log.i(TAG, "DOWN controlId=" + hit);
                        InputDeviceManager.getInstance().nativeVirtualButtonInput(hit, true);
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (activeControlId == ANALOG && analogActive) {
                    updateAnalog(x, y);
                    return true;
                }
                return pressed;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pressed) {
                    if (activeControlId == ANALOG) {
                        analogActive = false;
                        analogX = 0f;
                        analogY = 0f;
                        InputDeviceManager.getInstance().virtualJoystick(0f, 0f);
                        Log.i(TAG, "ANALOG reset x=0 y=0");
                    } else {
                        Log.i(TAG, "UP controlId=" + activeControlId);
                        InputDeviceManager.getInstance().nativeVirtualButtonInput(activeControlId, false);
                    }

                    activeControlId = -1;
                    pressed = false;
                    return true;
                }
                break;
        }

        return false;
    }

    private static void updateAnalog(float x, float y) {
        float cx = analogAreaX + analogAreaSize * 0.5f;
        float cy = analogAreaY + analogAreaSize * 0.5f;
        float radius = analogAreaSize * 0.5f;

        analogX = clamp((x - cx) / radius, -1f, 1f);
        analogY = clamp((y - cy) / radius, -1f, 1f);

        Log.i(TAG, "ANALOG x=" + analogX + " y=" + analogY);
        InputDeviceManager.getInstance().virtualJoystick(analogX, analogY);
    }

    public static void draw(Canvas canvas) {
        if (canvas == null || !visible || screenWidth <= 0 || screenHeight <= 0) return;

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(36f);

        drawRect(canvas, lRect, "L", BTN_L);
        drawRect(canvas, rRect, "R", BTN_R);

        drawAnalog(canvas);

        drawRect(canvas, leftRect, "←", BTN_LEFT);
        drawRect(canvas, rightRect, "→", BTN_RIGHT);
        drawRect(canvas, upRect, "↑", BTN_UP);
        drawRect(canvas, downRect, "↓", BTN_DOWN);

        drawRect(canvas, startRect, "START", BTN_START);

        drawCircle(canvas, ax, ay, "A", BTN_A);
        drawCircle(canvas, bx, by, "B", BTN_B);
        drawCircle(canvas, xx, xy, "X", BTN_X);
        drawCircle(canvas, yx, yy, "Y", BTN_Y);
    }

    private static void drawRect(Canvas canvas, RectF rect, String label, int id) {
        boolean active = activeControlId == id && pressed;

        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(active ? 210 : 130, 255, 0, 0);
        canvas.drawRoundRect(rect, 14f, 14f, paint);

        paint.setARGB(240, 255, 255, 255);
        canvas.drawText(label, rect.centerX(), rect.centerY() + 12f, paint);
    }

    private static void drawCircle(Canvas canvas, float cx, float cy, String label, int id) {
        boolean active = activeControlId == id && pressed;
        float radius = faceButtonSize * 0.5f;

        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(active ? 210 : 130, 0, 120, 255);
        canvas.drawCircle(cx + radius, cy + radius, radius, paint);

        paint.setARGB(240, 255, 255, 255);
        canvas.drawText(label, cx + radius, cy + radius + 12f, paint);
    }

    private static void drawAnalog(Canvas canvas) {
        float cx = analogAreaX + analogAreaSize * 0.4f;
        float cy = analogAreaY + analogAreaSize * 0.5f;
        float radius = analogAreaSize * 0.3f;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        paint.setARGB(180, 255, 255, 255);
        canvas.drawCircle(cx, cy, radius, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(170, 0, 255, 0);

        float moveRadius = radius * 0.72f;
        float knobX = cx + analogX * moveRadius;
        float knobY = cy + analogY * moveRadius;
        canvas.drawCircle(knobX, knobY, analogStickSize * 0.3f, paint);

        paint.setARGB(230, 255, 255, 255);
        canvas.drawText("ANALOG", cx, cy + moveRadius + 90f, paint);
    }

    private static boolean inAnalogArea(float x, float y) {
        float cx = analogAreaX + analogAreaSize * 0.5f;
        float cy = analogAreaY + analogAreaSize * 0.5f;
        float radius = analogAreaSize * 0.5f;

        float dx = x - cx;
        float dy = y - cy;

        return dx * dx + dy * dy <= radius * radius;
    }

    private static boolean inButtonCircle(float x, float y, float bx, float by) {
        float radius = faceButtonSize * 0.5f;
        float cx = bx + radius;
        float cy = by + radius;

        float dx = x - cx;
        float dy = y - cy;

        return dx * dx + dy * dy <= radius * radius;
    }

    public static void scaleElement(int elementId, float scale) {
        Log.i(TAG, "scaleElement elementId=" + elementId + " scale=" + scale);
    }

    public static void translateElement(int activeElemId, float dx, float dy) {
        Log.i(TAG, "translateElement activeElemId=" + activeElemId
                + " dx=" + dx + " dy=" + dy);
    }

    private static boolean virtualGamepadRegistered = false;

    private static void ensureVirtualGamepadRegistered() {
        if (virtualGamepadRegistered) return;

        InputDeviceManager.getInstance().joystickAdded(
                InputDeviceManager.GAMEPAD_ID,
                "Virtual Gamepad",
                0,
                "virtual_gamepad",
                null,
                null,
                false
        );

        virtualGamepadRegistered = true;
    }


    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
