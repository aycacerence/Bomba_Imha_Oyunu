package com.example.codedefuse;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

public class LowTimeWarningOverlay {

    private static final int WARNING_SECONDS = 150;
    private static final long CHECK_INTERVAL_MS = 250L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final View overlay;
    private ObjectAnimator flashAnimator;
    private boolean running = false;

    private final Runnable warningCheckRunnable = new Runnable() {
        @Override
        public void run() {
            updateWarningState();

            if (running) {
                handler.postDelayed(this, CHECK_INTERVAL_MS);
            }
        }
    };

    public LowTimeWarningOverlay(Activity activity) {
        FrameLayout contentRoot = activity.findViewById(android.R.id.content);
        overlay = new View(activity);
        overlay.setBackgroundColor(Color.argb(190, 255, 0, 0));
        overlay.setAlpha(0f);
        overlay.setClickable(false);
        overlay.setVisibility(View.GONE);

        contentRoot.addView(
                overlay,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );
    }

    public void start() {
        if (running) {
            return;
        }

        running = true;
        handler.post(warningCheckRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(warningCheckRunnable);
        stopFlash();
    }

    private void updateWarningState() {
        if (!GameState.isGameOver()
                && GameState.remainingTime > 0
                && GameState.remainingTime <= WARNING_SECONDS) {
            startFlash();
        } else {
            stopFlash();
        }
    }

    private void startFlash() {
        if (flashAnimator != null) {
            return;
        }

        overlay.setVisibility(View.VISIBLE);
        overlay.bringToFront();

        flashAnimator = ObjectAnimator.ofFloat(overlay, View.ALPHA, 0f, 0.55f);
        flashAnimator.setDuration(320);
        flashAnimator.setRepeatMode(ValueAnimator.REVERSE);
        flashAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flashAnimator.start();
    }

    private void stopFlash() {
        if (flashAnimator != null) {
            flashAnimator.cancel();
            flashAnimator = null;
        }

        overlay.setAlpha(0f);
        overlay.setVisibility(View.GONE);
    }
}
