package com.example.codedefuse;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class ColorMemoryGuideActivity extends Activity {

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_memory_guide);

        backButton = findViewById(R.id.btnBackFromColorMemoryGuide);
        backButton.setOnClickListener(v -> finish());
    }
}
