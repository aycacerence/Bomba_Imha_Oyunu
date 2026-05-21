package com.example.codedefuse;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BombActivity extends Activity {

    private static final int LOW_TIME_WARNING_SECONDS = 150;
    private static final long EXPLOSION_PREVIEW_MS = 2000L;

    private SevenSegmentTimerView timerView;
    private TextView mistakeText;
    private TextView solvedCountText;
    private TextView serialText;
    private TextView batteryText;
    private TextView indicatorText;
    private Button exitButton;

    private LinearLayout wireCard;
    private LinearLayout buttonCard;
    private LinearLayout colorMemoryCard;
    private LinearLayout dialCard;
    private LinearLayout passwordCard;

    private TextView wireStatusText;
    private TextView buttonStatusText;
    private TextView colorMemoryStatusText;
    private TextView dialStatusText;
    private TextView passwordStatusText;
    private View redWarningOverlay;
    private View explosionOverlay;

    private CountDownTimer countDownTimer;
    private ObjectAnimator warningFlashAnimator;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingResultRunnable;
    private boolean resultOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keepContentOutOfSystemBars();
        setContentView(R.layout.activity_bomb);

        if (GameState.serialNumber == null || GameState.serialNumber.isEmpty()) {
            GameState.setupGame("Oyuncu");
        }

        connectViews();
        setCardClicks();
        updateScreen();
        startTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScreen();
        checkGameStatus();
    }

    private void connectViews() {
        timerView = findViewById(R.id.txtTimer);
        mistakeText = findViewById(R.id.txtMistakes);
        solvedCountText = findViewById(R.id.txtSolvedCount);
        serialText = findViewById(R.id.txtSerialNumber);
        batteryText = findViewById(R.id.txtBatteryCount);
        indicatorText = findViewById(R.id.txtIndicatorCode);
        exitButton = findViewById(R.id.btnExitBomb);

        wireCard = findViewById(R.id.cardWireModule);
        buttonCard = findViewById(R.id.cardButtonModule);
        colorMemoryCard = findViewById(R.id.cardColorMemoryModule);
        dialCard = findViewById(R.id.cardDialModule);
        passwordCard = findViewById(R.id.cardPasswordModule);

        wireStatusText = findViewById(R.id.txtWireStatus);
        buttonStatusText = findViewById(R.id.txtButtonStatus);
        colorMemoryStatusText = findViewById(R.id.txtColorMemoryStatus);
        dialStatusText = findViewById(R.id.txtDialStatus);
        passwordStatusText = findViewById(R.id.txtPasswordStatus);
        redWarningOverlay = findViewById(R.id.redWarningOverlay);
        explosionOverlay = findViewById(R.id.explosionOverlay);
    }

    private void setCardClicks() {
        wireCard.setOnClickListener(v -> openModule(WireModuleActivity.class, GameState.isWireSolved));
        buttonCard.setOnClickListener(v -> openModule(ButtonModuleActivity.class, GameState.isButtonSolved));
        colorMemoryCard.setOnClickListener(v -> openModule(ColorMemoryModuleActivity.class, GameState.isColorMemorySolved));
        dialCard.setOnClickListener(v -> openModule(DialModuleActivity.class, GameState.isDialSolved));
        passwordCard.setOnClickListener(v -> openModule(PasswordModuleActivity.class, GameState.isPasswordSolved));
        exitButton.setOnClickListener(v -> showExitDialog());
    }

    private void startTimer() {
        if (GameState.isGameOver()) {
            return;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(GameState.remainingTime * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                GameState.remainingTime = (int) (millisUntilFinished / 1000);
                updateTopInfo();
                checkGameStatus();
            }

            @Override
            public void onFinish() {
                GameState.remainingTime = 0;
                GameState.isGameLost = true;
                updateScreen();
                openResult("time");
            }
        };

        countDownTimer.start();
    }

    private void openModule(Class<?> activityClass, boolean isSolved) {
        if (isSolved) {
            Toast.makeText(this, "Bu modül zaten çözüldü.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    private void updateScreen() {
        updateTopInfo();

        serialText.setText("Seri No: " + GameState.serialNumber);
        batteryText.setText("Pil Sayısı: " + GameState.batteryCount);
        indicatorText.setText("Indicator: " + GameState.indicatorCode);

        updateModuleCard(wireCard, wireStatusText, GameState.isWireSolved);
        updateModuleCard(buttonCard, buttonStatusText, GameState.isButtonSolved);
        updateModuleCard(colorMemoryCard, colorMemoryStatusText, GameState.isColorMemorySolved);
        updateModuleCard(dialCard, dialStatusText, GameState.isDialSolved);
        updateModuleCard(passwordCard, passwordStatusText, GameState.isPasswordSolved);
    }

    private void updateTopInfo() {
        timerView.setTime(GameState.remainingTime, GameState.totalTime);
        mistakeText.setText("Hata " + GameState.mistakeCount + "/" + GameState.maxMistakeCount);
        solvedCountText.setText("Çözülen Modül: " + GameState.solvedModuleCount + "/" + GameState.totalModuleCount);
        updateLowTimeWarning();
    }

    private void updateModuleCard(LinearLayout card, TextView statusText, boolean isSolved) {
        if (isSolved) {
            card.setBackgroundResource(R.drawable.module_solved);
            statusText.setText("ÇÖZÜLDÜ ✓");
            statusText.setTextColor(getResources().getColor(R.color.neon_green));
            statusText.setBackground(null);
            statusText.setRotation(0f);
        } else {
            card.setBackgroundResource(R.drawable.module_unsolved);
            statusText.setText("AKTİF");
            statusText.setTextColor(getResources().getColor(R.color.neon_yellow));
            statusText.setBackground(null);
            statusText.setRotation(0f);
        }
    }

    private void checkGameStatus() {
        if (resultOpened) {
            return;
        }

        if (GameState.isAllModulesSolved()) {
            GameState.isGameWon = true;
            openResult("success");
        } else if (GameState.mistakeCount >= GameState.maxMistakeCount) {
            GameState.isGameLost = true;
            openResult("mistakes");
        } else if (GameState.remainingTime <= 0) {
            GameState.isGameLost = true;
            openResult("time");
        }
    }

    private void openResult(String reason) {
        if (resultOpened) {
            return;
        }

        resultOpened = true;
        stopLowTimeWarning();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (reason.equals("mistakes") || reason.equals("time")) {
            showExplosionThenOpenResult(reason);
            return;
        }

        launchResult(reason);
    }

    private void showExplosionThenOpenResult(String reason) {
        explosionOverlay.setVisibility(View.VISIBLE);
        explosionOverlay.bringToFront();

        pendingResultRunnable = () -> launchResult(reason);
        handler.postDelayed(pendingResultRunnable, EXPLOSION_PREVIEW_MS);
    }

    private void launchResult(String reason) {
        Intent intent = new Intent(BombActivity.this, ResultActivity.class);
        intent.putExtra("reason", reason);
        startActivity(intent);
        finish();
    }

    private void updateLowTimeWarning() {
        if (!resultOpened && !GameState.isGameOver()
                && GameState.remainingTime > 0
                && GameState.remainingTime <= LOW_TIME_WARNING_SECONDS) {
            startLowTimeWarning();
        } else {
            stopLowTimeWarning();
        }
    }

    private void startLowTimeWarning() {
        if (warningFlashAnimator != null) {
            return;
        }

        redWarningOverlay.setVisibility(View.VISIBLE);
        warningFlashAnimator = ObjectAnimator.ofFloat(redWarningOverlay, View.ALPHA, 0f, 0.55f);
        warningFlashAnimator.setDuration(320);
        warningFlashAnimator.setRepeatMode(ValueAnimator.REVERSE);
        warningFlashAnimator.setRepeatCount(ValueAnimator.INFINITE);
        warningFlashAnimator.start();
    }

    private void stopLowTimeWarning() {
        if (warningFlashAnimator != null) {
            warningFlashAnimator.cancel();
            warningFlashAnimator = null;
        }

        if (redWarningOverlay != null) {
            redWarningOverlay.setAlpha(0f);
            redWarningOverlay.setVisibility(View.GONE);
        }
    }

    private void keepContentOutOfSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (resultOpened) {
            return;
        }

        showExitDialog();
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Oyundan çık")
                .setMessage("Oyundan çıkmak istiyor musun?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    Intent intent = new Intent(BombActivity.this, RoleSelectionActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hayır", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        stopLowTimeWarning();

        if (pendingResultRunnable != null) {
            handler.removeCallbacks(pendingResultRunnable);
        }
    }
}
