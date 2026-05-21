package com.example.codedefuse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public class SevenSegmentTimerView extends View {

    private static final boolean[][] DIGITS = {
            {true, true, true, true, true, true, false},
            {false, true, true, false, false, false, false},
            {true, true, false, true, true, false, true},
            {true, true, true, true, false, false, true},
            {false, true, true, false, false, true, true},
            {true, false, true, true, false, true, true},
            {true, false, true, true, true, true, true},
            {true, true, true, false, false, false, false},
            {true, true, true, true, true, true, true},
            {true, true, true, true, false, true, true}
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int remainingTime = 300;

    public SevenSegmentTimerView(Context context) {
        super(context);
    }

    public SevenSegmentTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTime(int remainingTime, int totalTime) {
        this.remainingTime = Math.max(remainingTime, 0);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = dp(10);
        RectF outer = new RectF(padding, padding, getWidth() - padding, getHeight() - padding);
        drawCasing(canvas, outer);

        float inset = dp(9);
        RectF screen = new RectF(outer.left + inset, outer.top + inset, outer.right - inset, outer.bottom - inset);
        drawDisplay(canvas, screen);

        String time = formatTime(remainingTime);
        drawTime(canvas, screen, time);
    }

    private void drawCasing(Canvas canvas, RectF outer) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(43, 51, 62));
        canvas.drawRoundRect(outer, dp(7), dp(7), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.rgb(115, 125, 141));
        canvas.drawRoundRect(outer, dp(7), dp(7), paint);
    }

    private void drawDisplay(Canvas canvas, RectF screen) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(5, 7, 10));
        canvas.drawRoundRect(screen, dp(3), dp(3), paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.rgb(21, 26, 34));
        canvas.drawRoundRect(screen, dp(3), dp(3), paint);
    }

    private void drawTime(Canvas canvas, RectF screen, String time) {
        float gap = dp(6);
        float colonWidth = dp(16);
        float digitHeight = screen.height() * 0.76f;
        float digitWidth = Math.min((screen.width() - colonWidth - gap * 4f) / 4f, digitHeight * 0.58f);
        float totalWidth = digitWidth * 4f + colonWidth + gap * 4f;
        float startX = screen.centerX() - totalWidth / 2f;
        float startY = screen.centerY() - digitHeight / 2f;

        drawDigit(canvas, Character.digit(time.charAt(0), 10), startX, startY, digitWidth, digitHeight);
        startX += digitWidth + gap;
        drawDigit(canvas, Character.digit(time.charAt(1), 10), startX, startY, digitWidth, digitHeight);
        startX += digitWidth + gap;
        drawColon(canvas, startX, startY, colonWidth, digitHeight);
        startX += colonWidth + gap;
        drawDigit(canvas, Character.digit(time.charAt(3), 10), startX, startY, digitWidth, digitHeight);
        startX += digitWidth + gap;
        drawDigit(canvas, Character.digit(time.charAt(4), 10), startX, startY, digitWidth, digitHeight);
    }

    private void drawDigit(Canvas canvas, int digit, float x, float y, float width, float height) {
        boolean[] segments = DIGITS[Math.max(0, Math.min(9, digit))];
        float thickness = Math.max(dp(5), width * 0.16f);
        float radius = thickness / 2f;
        float verticalHeight = (height - thickness * 3f) / 2f;

        drawSegment(canvas, segments[0], x + thickness, y, width - thickness * 2f, thickness, radius);
        drawSegment(canvas, segments[1], x + width - thickness, y + thickness, thickness, verticalHeight, radius);
        drawSegment(canvas, segments[2], x + width - thickness, y + thickness * 2f + verticalHeight, thickness, verticalHeight, radius);
        drawSegment(canvas, segments[3], x + thickness, y + height - thickness, width - thickness * 2f, thickness, radius);
        drawSegment(canvas, segments[4], x, y + thickness * 2f + verticalHeight, thickness, verticalHeight, radius);
        drawSegment(canvas, segments[5], x, y + thickness, thickness, verticalHeight, radius);
        drawSegment(canvas, segments[6], x + thickness, y + thickness + verticalHeight, width - thickness * 2f, thickness, radius);
    }

    private void drawSegment(Canvas canvas, boolean active, float x, float y, float width, float height, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? Color.rgb(255, 0, 18) : Color.rgb(52, 13, 18));
        canvas.drawRoundRect(new RectF(x, y, x + width, y + height), radius, radius, paint);

        if (active) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(255, 70, 77));
            canvas.drawRoundRect(new RectF(x, y, x + width, y + height), radius, radius, paint);
        }
    }

    private void drawColon(Canvas canvas, float x, float y, float width, float height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 0, 18));
        float dotRadius = Math.min(width * 0.25f, dp(5));
        float centerX = x + width / 2f;
        canvas.drawCircle(centerX, y + height * 0.35f, dotRadius, paint);
        canvas.drawCircle(centerX, y + height * 0.65f, dotRadius, paint);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
