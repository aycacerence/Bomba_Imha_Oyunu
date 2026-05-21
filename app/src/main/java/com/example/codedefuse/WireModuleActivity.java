package com.example.codedefuse;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class WireModuleActivity extends Activity {

    private final String RED = "Kırmızı";
    private final String BLUE = "Mavi";
    private final String YELLOW = "Sarı";
    private final String WHITE = "Beyaz";
    private final String BLACK = "Siyah";

    private TextView serialText;
    private TextView infoText;
    private LinearLayout wireContainer;
    private Button backButton;

    private ArrayList<String> wires;
    private int correctWireIndex;
    private final Random random = new Random();
    private LowTimeWarningOverlay lowTimeWarningOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wire_module);
        lowTimeWarningOverlay = new LowTimeWarningOverlay(this);
        lowTimeWarningOverlay.start();

        serialText = findViewById(R.id.txtWireSerial);
        infoText = findViewById(R.id.txtWireInfo);
        wireContainer = findViewById(R.id.wireContainer);
        backButton = findViewById(R.id.btnBackFromWire);

        if (GameState.serialNumber == null || GameState.serialNumber.isEmpty()) {
            GameState.setupGame("Oyuncu");
        }

        serialText.setText("Seri No: " + GameState.serialNumber);
        backButton.setOnClickListener(v -> finish());

        prepareWires();
        showWires();
    }

    private void prepareWires() {
        if (GameState.wireDataGenerated) {
            wires = new ArrayList<>(GameState.wireColors);
            correctWireIndex = GameState.correctWireIndex;
        } else {
            wires = new ArrayList<>();
            String[] colors = {RED, BLUE, YELLOW, WHITE, BLACK};
            int wireCount = 3 + random.nextInt(4);

            for (int i = 0; i < wireCount; i++) {
                wires.add(colors[random.nextInt(colors.length)]);
            }

            correctWireIndex = findCorrectWire();
            
            // Save to GameState
            GameState.wireColors = new ArrayList<>(wires);
            GameState.correctWireIndex = correctWireIndex;
            GameState.wireDataGenerated = true;
        }
        
        infoText.setText(wires.size() + " kablo algılandı. Yukarıdan aşağıya sırayı uzmana söyle.");
    }

    private void showWires() {
        wireContainer.removeAllViews();

        for (int i = 0; i < wires.size(); i++) {
            final int wireIndex = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(10), dp(8));
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> cutWire(wireIndex));

            TextView orderText = new TextView(this);
            orderText.setText((i + 1) + ".");
            // Error fixed: using tech_text_dim which is defined in colors.xml
            orderText.setTextColor(getResources().getColor(R.color.tech_text_dim));
            orderText.setTextSize(15);
            orderText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            row.addView(orderText, new LinearLayout.LayoutParams(dp(34), LinearLayout.LayoutParams.WRAP_CONTENT));

            View wireLine = new View(this);
            wireLine.setBackground(makeWireDrawable(wires.get(i)));
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(0, dp(14), 1);
            lineParams.setMargins(0, 0, dp(12), 0);
            row.addView(wireLine, lineParams);

            TextView colorText = new TextView(this);
            colorText.setText(wires.get(i));
            // Error fixed: using tech_text_main which is defined in colors.xml
            colorText.setTextColor(getResources().getColor(R.color.tech_text_main));
            colorText.setTextSize(14);
            row.addView(colorText, new LinearLayout.LayoutParams(dp(72), LinearLayout.LayoutParams.WRAP_CONTENT));

            wireContainer.addView(row);
        }
    }

    private void cutWire(int selectedIndex) {
        if (selectedIndex == correctWireIndex) {
            GameState.markWireSolved();
            Toast.makeText(this, "Kablo modülü çözüldü.", Toast.LENGTH_SHORT).show();
        } else {
            GameState.addMistake();
            Toast.makeText(this, "Yanlış kablo kesildi.", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private int findCorrectWire() {
        int count = wires.size();
        boolean lastSerialDigitOdd = GameState.getLastDigitOfSerial() % 2 == 1;

        if (count == 3) {
            if (countColor(RED) == 0) {
                return 1;
            } else if (wires.get(2).equals(WHITE)) {
                return 2;
            } else if (countColor(BLUE) > 1) {
                return lastIndexOf(BLUE);
            } else {
                return 2;
            }
        }

        if (count == 4) {
            if (countColor(RED) > 1 && lastSerialDigitOdd) {
                return lastIndexOf(RED);
            } else if (wires.get(3).equals(YELLOW) && countColor(RED) == 0) {
                return 0;
            } else if (countColor(BLUE) == 1) {
                return 0;
            } else if (countColor(YELLOW) > 1) {
                return 3;
            } else {
                return 1;
            }
        }

        if (count == 5) {
            if (wires.get(4).equals(BLACK) && lastSerialDigitOdd) {
                return 3;
            } else if (countColor(RED) == 1 && countColor(YELLOW) > 1) {
                return 0;
            } else if (countColor(BLACK) == 0) {
                return 1;
            } else {
                return 0;
            }
        }

        if (countColor(YELLOW) == 0 && lastSerialDigitOdd) {
            return 2;
        } else if (countColor(YELLOW) == 1 && countColor(WHITE) > 1) {
            return 3;
        } else if (countColor(RED) == 0) {
            return 5;
        } else {
            return 3;
        }
    }

    private int countColor(String color) {
        int count = 0;

        for (String wire : wires) {
            if (wire.equals(color)) {
                count++;
            }
        }

        return count;
    }

    private int lastIndexOf(String color) {
        for (int i = wires.size() - 1; i >= 0; i--) {
            if (wires.get(i).equals(color)) {
                return i;
            }
        }

        return wires.size() - 1;
    }

    private GradientDrawable makeWireDrawable(String colorName) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(8));

        if (colorName.equals(RED)) {
            drawable.setColor(Color.rgb(255, 49, 87));
        } else if (colorName.equals(BLUE)) {
            drawable.setColor(Color.rgb(53, 128, 255));
        } else if (colorName.equals(YELLOW)) {
            drawable.setColor(Color.rgb(255, 212, 71));
        } else if (colorName.equals(WHITE)) {
            drawable.setColor(Color.rgb(235, 238, 242));
        } else {
            drawable.setColor(Color.rgb(15, 18, 24));
            drawable.setStroke(dp(1), Color.rgb(80, 88, 104));
        }

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
