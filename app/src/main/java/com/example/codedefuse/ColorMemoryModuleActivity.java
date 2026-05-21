package com.example.codedefuse;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class ColorMemoryModuleActivity extends Activity {

    private final String RED = "Kırmızı";
    private final String BLUE = "Mavi";
    private final String GREEN = "Yeşil";
    private final String YELLOW = "Sarı";

    private TextView serialText;
    private TextView mistakeText;
    private TextView stageText;
    private TextView activeColorText;
    private View redPanel;
    private View bluePanel;
    private View greenPanel;
    private View yellowPanel;
    private Button redButton;
    private Button blueButton;
    private Button greenButton;
    private Button yellowButton;
    private Button backButton;

    private ArrayList<String> colorSequence;
    private int currentStage = 0;
    private final Random random = new Random();
    private LowTimeWarningOverlay lowTimeWarningOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_memory_module);
        lowTimeWarningOverlay = new LowTimeWarningOverlay(this);
        lowTimeWarningOverlay.start();

        if (GameState.isColorMemorySolved) {
            Toast.makeText(this, "Bu modül zaten çözüldü.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        connectViews();

        if (GameState.serialNumber == null || GameState.serialNumber.isEmpty()) {
            GameState.setupGame("Oyuncu");
        }

        prepareButtons();
        createColorSequence();
        updateScreen();

        redButton.setOnClickListener(v -> checkColor(RED));
        blueButton.setOnClickListener(v -> checkColor(BLUE));
        greenButton.setOnClickListener(v -> checkColor(GREEN));
        yellowButton.setOnClickListener(v -> checkColor(YELLOW));
        backButton.setOnClickListener(v -> finish());
    }

    private void connectViews() {
        serialText = findViewById(R.id.txtColorMemorySerial);
        mistakeText = findViewById(R.id.txtColorMemoryMistakes);
        stageText = findViewById(R.id.txtColorMemoryStage);
        activeColorText = findViewById(R.id.txtActiveColor);
        redPanel = findViewById(R.id.panelRed);
        bluePanel = findViewById(R.id.panelBlue);
        greenPanel = findViewById(R.id.panelGreen);
        yellowPanel = findViewById(R.id.panelYellow);
        redButton = findViewById(R.id.btnColorRed);
        blueButton = findViewById(R.id.btnColorBlue);
        greenButton = findViewById(R.id.btnColorGreen);
        yellowButton = findViewById(R.id.btnColorYellow);
        backButton = findViewById(R.id.btnBackFromColorMemory);
    }

    private void prepareButtons() {
        prepareSimonPanel();
        styleColorButton(redButton, RED);
        styleColorButton(blueButton, BLUE);
        styleColorButton(greenButton, GREEN);
        styleColorButton(yellowButton, YELLOW);
    }

    private void createColorSequence() {
        if (GameState.colorMemoryDataGenerated) {
            colorSequence = new ArrayList<>(GameState.colorSequence);
            currentStage = GameState.colorMemoryStep;
        } else {
            String[] colors = {RED, BLUE, GREEN, YELLOW};
            colorSequence = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                colorSequence.add(colors[random.nextInt(colors.length)]);
            }
            
            GameState.colorSequence = new ArrayList<>(colorSequence);
            GameState.colorMemoryDataGenerated = true;
            GameState.colorMemoryStep = 0;
            currentStage = 0;
        }
    }

    private void updateScreen() {
        if (currentStage >= colorSequence.size()) return;

        serialText.setText("Seri No: " + GameState.serialNumber);
        mistakeText.setText("Hata: " + GameState.mistakeCount + "/" + GameState.maxMistakeCount);
        stageText.setText("Aşama " + (currentStage + 1) + "/3");
        activeColorText.setText("");
        activeColorText.setBackground(null);
        showActivePanel(colorSequence.get(currentStage));
    }

    private void checkColor(String selectedColor) {
        if (currentStage >= colorSequence.size()) return;

        String correctColor = getCorrectColor(colorSequence.get(currentStage));

        if (!selectedColor.equals(correctColor)) {
            GameState.addMistake();
            Toast.makeText(this, "Yanlış renk seçildi.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentStage++;
        GameState.colorMemoryStep = currentStage;

        if (currentStage >= colorSequence.size()) {
            GameState.markColorMemorySolved();
            Toast.makeText(this, "Renk hafıza modülü çözüldü.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            updateScreen();
        }
    }

    private String getCorrectColor(String lightColor) {
        int mistakeTable = GameState.mistakeCount;

        if (mistakeTable > 2) {
            mistakeTable = 2;
        }

        if (GameState.hasVowelInSerial()) {
            return getColorWithVowel(lightColor, mistakeTable);
        } else {
            return getColorWithoutVowel(lightColor, mistakeTable);
        }
    }

    private String getColorWithVowel(String lightColor, int mistakeTable) {
        if (mistakeTable == 0) {
            if (lightColor.equals(RED)) return BLUE;
            if (lightColor.equals(BLUE)) return RED;
            if (lightColor.equals(GREEN)) return YELLOW;
            return GREEN;
        }

        if (mistakeTable == 1) {
            if (lightColor.equals(RED)) return YELLOW;
            if (lightColor.equals(BLUE)) return GREEN;
            if (lightColor.equals(GREEN)) return BLUE;
            return RED;
        }

        if (lightColor.equals(RED)) return GREEN;
        if (lightColor.equals(BLUE)) return RED;
        if (lightColor.equals(GREEN)) return YELLOW;
        return BLUE;
    }

    private String getColorWithoutVowel(String lightColor, int mistakeTable) {
        if (mistakeTable == 0) {
            if (lightColor.equals(RED)) return BLUE;
            if (lightColor.equals(BLUE)) return YELLOW;
            if (lightColor.equals(GREEN)) return GREEN;
            return RED;
        }

        if (mistakeTable == 1) {
            if (lightColor.equals(RED)) return RED;
            if (lightColor.equals(BLUE)) return BLUE;
            if (lightColor.equals(GREEN)) return YELLOW;
            return GREEN;
        }

        if (lightColor.equals(RED)) return YELLOW;
        if (lightColor.equals(BLUE)) return GREEN;
        if (lightColor.equals(GREEN)) return BLUE;
        return RED;
    }

    private void styleColorButton(Button button, String colorName) {
        button.setBackground(makeColorBox(colorName));
        button.setText(colorName);
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        if (colorName.equals(YELLOW)) {
            button.setTextColor(Color.rgb(10, 12, 16));
        } else {
            button.setTextColor(Color.WHITE);
        }
    }

    private void prepareSimonPanel() {
        resetSimonPanel();
    }

    private void showActivePanel(String colorName) {
        resetSimonPanel();

        View activePanel = getPanelView(colorName);
        activePanel.setBackground(makeSimonPiece(colorName, true));
        activePanel.setAlpha(1f);
        activePanel.setElevation(dp(4));
        activePanel.bringToFront();
        activePanel.animate().cancel();
        activePanel.setScaleX(1f);
        activePanel.setScaleY(1f);
    }

    private void resetSimonPanel() {
        resetSimonPiece(redPanel, RED);
        resetSimonPiece(bluePanel, BLUE);
        resetSimonPiece(greenPanel, GREEN);
        resetSimonPiece(yellowPanel, YELLOW);
    }

    private void resetSimonPiece(View panel, String colorName) {
        panel.animate().cancel();
        panel.setScaleX(1f);
        panel.setScaleY(1f);
        panel.setAlpha(0.58f);
        panel.setElevation(dp(2));
        panel.setBackground(makeSimonPiece(colorName, false));
    }

    private View getPanelView(String colorName) {
        if (colorName.equals(RED)) return redPanel;
        if (colorName.equals(BLUE)) return bluePanel;
        if (colorName.equals(GREEN)) return greenPanel;
        return yellowPanel;
    }

    private GradientDrawable makeSimonPiece(String colorName, boolean active) {
        int baseColor = active ? getBrightColorValue(colorName) : getDimColorValue(colorName);
        int shadowColor = active ? getColorValue(colorName) : getDeepColorValue(colorName);
        int strokeColor = active ? Color.rgb(240, 245, 250) : Color.rgb(24, 28, 34);
        int strokeWidth = active ? dp(2) : dp(2);

        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{baseColor, shadowColor});
        drawable.setCornerRadius(dp(9));
        drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    private GradientDrawable makeColorBox(String colorName) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(12));
        drawable.setStroke(dp(2), Color.rgb(240, 245, 250));
        drawable.setColor(getColorValue(colorName));
        return drawable;
    }

    private int getColorValue(String colorName) {
        if (colorName.equals(RED)) {
            return Color.rgb(255, 49, 87);
        } else if (colorName.equals(BLUE)) {
            return Color.rgb(53, 128, 255);
        } else if (colorName.equals(GREEN)) {
            return Color.rgb(53, 255, 138);
        } else {
            return Color.rgb(255, 212, 71);
        }
    }

    private int getDimColorValue(String colorName) {
        if (colorName.equals(RED)) {
            return Color.rgb(154, 18, 38);
        } else if (colorName.equals(BLUE)) {
            return Color.rgb(10, 35, 154);
        } else if (colorName.equals(GREEN)) {
            return Color.rgb(19, 139, 40);
        } else {
            return Color.rgb(186, 143, 20);
        }
    }

    private int getDeepColorValue(String colorName) {
        if (colorName.equals(RED)) {
            return Color.rgb(70, 10, 20);
        } else if (colorName.equals(BLUE)) {
            return Color.rgb(6, 20, 86);
        } else if (colorName.equals(GREEN)) {
            return Color.rgb(8, 71, 23);
        } else {
            return Color.rgb(103, 78, 11);
        }
    }

    private int getBrightColorValue(String colorName) {
        if (colorName.equals(RED)) {
            return Color.rgb(255, 119, 135);
        } else if (colorName.equals(BLUE)) {
            return Color.rgb(143, 194, 255);
        } else if (colorName.equals(GREEN)) {
            return Color.rgb(139, 255, 172);
        } else {
            return Color.rgb(255, 244, 136);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (lowTimeWarningOverlay != null) {
            lowTimeWarningOverlay.stop();
        }

        super.onDestroy();
    }
}
