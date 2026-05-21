package com.example.codedefuse;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class WireGuideActivity extends Activity {

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wire_guide);

        backButton = findViewById(R.id.btnBackFromWireGuide);
        backButton.setOnClickListener(v -> finish());
    }
}
