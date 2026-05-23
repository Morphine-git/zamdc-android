package com.flycast.emulator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class ClassicVirtualControllerView extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean visible = true;

    public ClassicVirtualControllerView(Context ctx) {
        super(ctx);
        setWillNotDraw(false);
    }

    public void setVisible(boolean v) {
        visible = v;
        setAlpha(v ? 1f : 0f);
    }

    @Override
    protected void onDraw(Canvas c) {
        if (!visible) return;

        float w = getWidth();
        float h = getHeight();

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(3f, w * 0.004f));
        p.setARGB(130, 220, 220, 220);

        // --- Left analog box ---
        float boxSize = w * 0.20f;
        RectF analogBox = new RectF(w * 0.06f, h * 0.18f, w * 0.06f + boxSize, h * 0.18f + boxSize);
        c.drawRoundRect(analogBox, 20, 20, p);
        c.drawCircle(analogBox.centerX(), analogBox.centerY(), boxSize * 0.18f, p);

        // --- D-pad (left bottom) ---
        float dpadSize = w * 0.22f;
        RectF dpadBox = new RectF(w * 0.06f, h * 0.58f, w * 0.06f + dpadSize, h * 0.58f + dpadSize);
        c.drawRoundRect(dpadBox, 20, 20, p);

        // "D" letter
        p.setStyle(Paint.Style.FILL);
        p.setARGB(130, 200, 200, 200);
        p.setTextSize(w * 0.035f);
        c.drawText("D", dpadBox.centerX() - p.measureText("D") / 2f, dpadBox.centerY() + w * 0.012f, p);

        // --- START center ---
        p.setStyle(Paint.Style.STROKE);
        p.setARGB(130, 220, 220, 220);
        float startW = w * 0.18f;
        float startH = h * 0.08f;
        RectF startBox = new RectF(w * 0.41f, h * 0.72f, w * 0.41f + startW, h * 0.72f + startH);
        c.drawRoundRect(startBox, 20, 20, p);

        p.setStyle(Paint.Style.FILL);
        p.setARGB(130, 200, 200, 200);
        p.setTextSize(w * 0.030f);
        c.drawText("START",
                startBox.centerX() - p.measureText("START") / 2f,
                startBox.centerY() + w * 0.010f,
                p);

        // --- Right buttons A/B/X/Y ---
        float btnR = w * 0.06f;
        float cx = w * 0.86f;
        float cy = h * 0.60f;

        drawCircleButton(c, cx, cy - btnR * 1.2f, btnR, "Y");
        drawCircleButton(c, cx - btnR * 1.2f, cy, btnR, "X");
        drawCircleButton(c, cx + btnR * 1.2f, cy, btnR, "B");
        drawCircleButton(c, cx, cy + btnR * 1.2f, btnR, "A");

        // --- LT RT label ---
        p.setARGB(110, 200, 200, 200);
        p.setTextSize(w * 0.040f);
        c.drawText("LT", w * 0.76f, h * 0.46f, p);
        c.drawText("RT", w * 0.86f, h * 0.46f, p);
    }

    private void drawCircleButton(Canvas c, float x, float y, float r, String txt) {
        p.setStyle(Paint.Style.STROKE);
        p.setARGB(130, 220, 220, 220);
        c.drawCircle(x, y, r, p);

        p.setStyle(Paint.Style.FILL);
        p.setARGB(130, 200, 200, 200);
        p.setTextSize(r * 0.9f);
        c.drawText(txt, x - p.measureText(txt) / 2f, y + r * 0.33f, p);
    }
}

