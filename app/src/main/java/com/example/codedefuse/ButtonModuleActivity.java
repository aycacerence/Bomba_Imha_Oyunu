package com.example.codedefuse;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class ButtonModuleActivity extends Activity {

    private final String BLUE = "Mavi";
    private final String RED = "Kırmızı";
    private final String YELLOW = "Sarı";
    private final String WHITE = "Beyaz";

    private TextView batteryText;
    private TextView indicatorText;
    private TextView buttonInfoText;
    private TextView bigButtonText;
    private TextView stripText;
    private TextView releaseCounterText;
    private LinearLayout holdArea;
    private Button quickPressButton;
    private Button holdPressButton;
    private Button releaseButton;
    private Button backButton;

    private String buttonColor;
    private String buttonWord;
    private String correctAction;
    private String stripColor;
    private int currentReleaseNumber = 0;
    private LowTimeWarningOverlay lowTimeWarningOverlay;
    private final Random random = new Random();
    private final Handler releaseCounterHandler = new Handler(Looper.getMainLooper());
    private final Runnable releaseCounterRunnable = new Runnable() {
        @Override
        public void run() {
            currentReleaseNumber = (currentReleaseNumber + 1) % 10;
            releaseCounterText.setText(String.valueOf(currentReleaseNumber));
            releaseCounterHandler.postDelayed(this, 800);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_module);
        lowTimeWarningOverlay = new LowTimeWarningOverlay(this);
        lowTimeWarningOverlay.start();

        connectViews();

        if (GameState.serialNumber == null || GameState.serialNumber.isEmpty()) {
            GameState.setupGame("Oyuncu");
        }

        prepareButton();
        showButtonInfo();

        quickPressButton.setOnClickListener(v -> checkMainAction("Hemen Bas"));
        holdPressButton.setOnClickListener(v -> checkMainAction("Basılı Tut"));
        releaseButton.setOnClickListener(v -> checkReleaseNumber());
        backButton.setOnClickListener(v -> finish());
    }

    private void connectViews() {
        batteryText = findViewById(R.id.txtButtonBattery);
        indicatorText = findViewById(R.id.txtButtonIndicator);
        buttonInfoText = findViewById(R.id.txtButtonInfo);
        bigButtonText = findViewById(R.id.txtBigButton);
        stripText = findViewById(R.id.txtStripColor);
        releaseCounterText = findViewById(R.id.txtReleaseCounter);
        holdArea = findViewById(R.id.holdArea);
        quickPressButton = findViewById(R.id.btnQuickPress);
        holdPressButton = findViewById(R.id.btnHoldPress);
        releaseButton = findViewById(R.id.btnReleaseButton);
        backButton = findViewById(R.id.btnBackFromButton);
    }

    private void prepareButton() {
        if (GameState.buttonDataGenerated) {
            buttonColor = GameState.buttonColor;
            buttonWord = GameState.buttonText;
            stripColor = GameState.stripColor;
        } else {
            String[] colors = {BLUE, RED, YELLOW, WHITE};
            String[] words = {"PATLAT", "İPTAL", "BAS", "TUT"};

            buttonColor = colors[random.nextInt(colors.length)];
            buttonWord = words[random.nextInt(words.length)];
            
            GameState.buttonColor = buttonColor;
            GameState.buttonText = buttonWord;
            GameState.buttonDataGenerated = true;
        }
        correctAction = findCorrectAction();
    }

    private void showButtonInfo() {
        batteryText.setText("Pil Sayısı: " + GameState.batteryCount);
        indicatorText.setText("Indicator: " + GameState.indicatorCode);
        buttonInfoText.setText("Düğme: " + buttonColor + " / " + buttonWord);

        bigButtonText.setText(buttonWord);
        bigButtonText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bigButtonText.setBackground(makeCircle(buttonColor));

        bigButtonText.setTextColor(Color.rgb(10, 12, 16));

        if (GameState.stripColor != null && !GameState.stripColor.isEmpty()) {
            showHoldStep(GameState.stripColor);
        }
    }

    private String findCorrectAction() {
        if (buttonColor.equals(BLUE) && buttonWord.equals("İPTAL")) {
            return "Basılı Tut";
        } else if (GameState.batteryCount > 1 && buttonWord.equals("PATLAT")) {
            return "Hemen Bas";
        } else if (buttonColor.equals(WHITE) && GameState.indicatorCode.equals("CAR")) {
            return "Basılı Tut";
        } else if (GameState.batteryCount > 2 && GameState.indicatorCode.equals("FRK")) {
            return "Hemen Bas";
        } else if (buttonColor.equals(YELLOW)) {
            return "Basılı Tut";
        } else if (buttonColor.equals(RED) && buttonWord.equals("BAS")) {
            return "Hemen Bas";
        } else {
            return "Basılı Tut";
        }
    }

    private void checkMainAction(String selectedAction) {
        if (!selectedAction.equals(correctAction)) {
            wrongMove();
            return;
        }

        if (correctAction.equals("Hemen Bas")) {
            solveModule();
        } else {
            String[] stripColors = {BLUE, WHITE, YELLOW, RED};
            stripColor = stripColors[random.nextInt(stripColors.length)];
            GameState.stripColor = stripColor;
            showHoldStep(stripColor);
        }
    }

    private void showHoldStep(String color) {
        stripColor = color;
        stripText.setText("Şerit Rengi: " + stripColor);
        stripText.setBackground(makeStrip(stripColor));
        holdArea.setVisibility(View.VISIBLE);
        quickPressButton.setEnabled(false);
        holdPressButton.setEnabled(false);
        startReleaseCounter();
    }

    private void checkReleaseNumber() {
        if (currentReleaseNumber == getCorrectReleaseNumber()) {
            solveModule();
        } else {
            wrongMove();
        }
    }

    private void startReleaseCounter() {
        releaseCounterHandler.removeCallbacks(releaseCounterRunnable);
        currentReleaseNumber = random.nextInt(10);
        releaseCounterText.setText(String.valueOf(currentReleaseNumber));
        releaseCounterHandler.postDelayed(releaseCounterRunnable, 800);
    }

    private int getCorrectReleaseNumber() {
        if (stripColor.equals(BLUE)) {
            return 4;
        } else if (stripColor.equals(WHITE)) {
            return 1;
        } else if (stripColor.equals(YELLOW)) {
            return 5;
        } else {
            return 1;
        }
    }

    private void solveModule() {
        releaseCounterHandler.removeCallbacks(releaseCounterRunnable);
        GameState.markButtonSolved();
        Toast.makeText(this, "Büyük düğme modülü çözüldü.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void wrongMove() {
        releaseCounterHandler.removeCallbacks(releaseCounterRunnable);
        GameState.addMistake();
        Toast.makeText(this, "Yanlış işlem.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (lowTimeWarningOverlay != null) {
            lowTimeWarningOverlay.stop();
        }

        releaseCounterHandler.removeCallbacks(releaseCounterRunnable);
        super.onDestroy();
    }

    private GradientDrawable makeCircle(String colorName) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setStroke(dp(3), Color.rgb(255, 255, 255));
        drawable.setColor(getColorValue(colorName));
        return drawable;
    }

    private GradientDrawable makeStrip(String colorName) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(8));
        drawable.setColor(getColorValue(colorName));

        if (colorName.equals(WHITE)) {
            drawable.setStroke(dp(1), Color.rgb(190, 198, 210));
        }

        return drawable;
    }

    private int getColorValue(String colorName) {
        if (colorName.equals(BLUE)) {
            return Color.rgb(53, 128, 255);
        } else if (colorName.equals(RED)) {
            return Color.rgb(255, 49, 87);
        } else if (colorName.equals(YELLOW)) {
            return Color.rgb(255, 212, 71);
        } else {
            return Color.rgb(235, 238, 242);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
