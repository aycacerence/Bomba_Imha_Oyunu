package com.example.codedefuse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends Activity {

    private View startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        startButton = findViewById(R.id.btnStartMission);

        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
        });
    }
}
