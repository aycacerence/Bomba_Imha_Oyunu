package com.example.codedefuse;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class PasswordGuideActivity extends Activity {

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_guide);

        backButton = findViewById(R.id.btnBackFromPasswordGuide);
        backButton.setOnClickListener(v -> finish());
    }
}
