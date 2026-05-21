package com.example.codedefuse;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DialModuleActivity extends Activity {

    private final String UP = "YUKARI";
    private final String RIGHT = "SAĞ";
    private final String DOWN = "AŞAĞI";
    private final String LEFT = "SOL";

    private TextView serialText;
    private TextView batteryText;
    private TextView indicatorText;
    private TextView stepText;
    private TextView firstSlotText;
    private TextView secondSlotText;
    private TextView thirdSlotText;
    private Button upButton;
    private Button rightButton;
    private Button downButton;
    private Button leftButton;
    private Button backButton;

    private ArrayList<String> correctSequence;
    private int currentStep = 0;
    private LowTimeWarningOverlay lowTimeWarningOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial_module);
        lowTimeWarningOverlay = new LowTimeWarningOverlay(this);
        lowTimeWarningOverlay.start();

        if (GameState.isDialSolved) {
            Toast.makeText(this, "Bu modül zaten çözüldü.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        connectViews();

        if (GameState.serialNumber == null || GameState.serialNumber.isEmpty()) {
            GameState.setupGame("Oyuncu");
        }

        prepareDialSequence();
        prepareButtons();
        updateScreen();

        upButton.setOnClickListener(v -> checkDirection(UP));
        rightButton.setOnClickListener(v -> checkDirection(RIGHT));
        downButton.setOnClickListener(v -> checkDirection(DOWN));
        leftButton.setOnClickListener(v -> checkDirection(LEFT));
        backButton.setOnClickListener(v -> finish());
    }

    private void connectViews() {
        serialText = findViewById(R.id.txtDialSerial);
        batteryText = findViewById(R.id.txtDialBattery);
        indicatorText = findViewById(R.id.txtDialIndicator);
        stepText = findViewById(R.id.txtDialStep);
        firstSlotText = findViewById(R.id.txtDialSlotOne);
        secondSlotText = findViewById(R.id.txtDialSlotTwo);
        thirdSlotText = findViewById(R.id.txtDialSlotThree);
        upButton = findViewById(R.id.btnDialUp);
        rightButton = findViewById(R.id.btnDialRight);
        downButton = findViewById(R.id.btnDialDown);
        leftButton = findViewById(R.id.btnDialLeft);
        backButton = findViewById(R.id.btnBackFromDial);
    }

    private void prepareDialSequence() {
        if (GameState.dialDataGenerated) {
            correctSequence = new ArrayList<>(GameState.dialCorrectSequence);
            currentStep = GameState.dialStep;
            return;
        }

        correctSequence = createCorrectSequence();
        GameState.dialCorrectSequence = new ArrayList<>(correctSequence);
        GameState.dialDataGenerated = true;
        GameState.dialStep = 0;
        currentStep = 0;
    }

    private ArrayList<String> createCorrectSequence() {
        ArrayList<String> sequence = new ArrayList<>();

        if (GameState.indicatorCode.equals("CAR")) {
            sequence.add(RIGHT);
            sequence.add(UP);
            sequence.add(LEFT);
        } else if (GameState.batteryCount == 0) {
            sequence.add(LEFT);
            sequence.add(DOWN);
            sequence.add(RIGHT);
        } else if (GameState.getLastDigitOfSerial() % 2 == 1) {
            sequence.add(UP);
            sequence.add(RIGHT);
            sequence.add(DOWN);
        } else if (GameState.hasVowelInSerial()) {
            sequence.add(DOWN);
            sequence.add(LEFT);
            sequence.add(UP);
        } else {
            sequence.add(LEFT);
            sequence.add(UP);
            sequence.add(RIGHT);
        }

        return sequence;
    }

    private void prepareButtons() {
        styleDirectionButton(upButton);
        styleDirectionButton(rightButton);
        styleDirectionButton(downButton);
        styleDirectionButton(leftButton);
    }

    private void updateScreen() {
        serialText.setText("Seri No: " + GameState.serialNumber);
        batteryText.setText("Pil Sayısı: " + GameState.batteryCount);
        indicatorText.setText("Indicator: " + GameState.indicatorCode);
        stepText.setText("Adım " + (currentStep + 1) + "/3");
        updateSequenceSlots();
    }

    private void checkDirection(String selectedDirection) {
        if (currentStep >= correctSequence.size()) {
            return;
        }

        if (!selectedDirection.equals(correctSequence.get(currentStep))) {
            GameState.addMistake();
            Toast.makeText(this, "Yanlış yön seçildi.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentStep++;
        GameState.dialStep = currentStep;
        updateSequenceSlots();

        if (currentStep >= correctSequence.size()) {
            GameState.markDialSolved();
            Toast.makeText(this, "Kadran modülü imha edildi.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            updateScreen();
        }
    }

    private void styleDirectionButton(Button button) {
        button.setBackground(makeDirectionButton());
        button.setTextColor(Color.rgb(242, 245, 247));
        button.setTextSize(32);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    }

    private void updateSequenceSlots() {
        resetSlot(firstSlotText);
        resetSlot(secondSlotText);
        resetSlot(thirdSlotText);

        if (currentStep > 0) {
            fillSlot(firstSlotText, correctSequence.get(0));
        }

        if (currentStep > 1) {
            fillSlot(secondSlotText, correctSequence.get(1));
        }

        if (currentStep > 2) {
            fillSlot(thirdSlotText, correctSequence.get(2));
        }
    }

    private void resetSlot(TextView slotText) {
        slotText.setText("·");
        slotText.setTextColor(Color.rgb(115, 125, 141));
    }

    private void fillSlot(TextView slotText, String direction) {
        slotText.setText(getArrowSymbol(direction));
        slotText.setTextColor(Color.rgb(53, 255, 138));
    }

    private String getArrowSymbol(String direction) {
        if (direction.equals(UP)) return "↑";
        if (direction.equals(RIGHT)) return "→";
        if (direction.equals(DOWN)) return "↓";
        return "←";
    }

    private GradientDrawable makeDirectionButton() {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(42, 50, 64), Color.rgb(17, 23, 34)});
        drawable.setCornerRadius(dp(12));
        drawable.setStroke(dp(2), Color.rgb(255, 212, 71));
        return drawable;
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
