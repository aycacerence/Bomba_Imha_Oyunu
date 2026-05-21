package com.example.codedefuse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DefuserSetupActivity extends Activity {

    private EditText usernameEditText;
    private Button startGameButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defuser_setup);

        usernameEditText = findViewById(R.id.editUsername);
        startGameButton = findViewById(R.id.btnStartGame);
        backButton = findViewById(R.id.btnBackToRoles);

        if (PreferencesHelper.hasUsername(this)) {
            usernameEditText.setText(PreferencesHelper.getUsername(this));
        }

        startGameButton.setOnClickListener(v -> startGame());

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(DefuserSetupActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void startGame() {
        String username = usernameEditText.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Lütfen kullanıcı adını gir.", Toast.LENGTH_SHORT).show();
            return;
        }

        PreferencesHelper.saveUsername(this, username);
        GameState.setupGame(username);

        Intent intent = new Intent(DefuserSetupActivity.this, BombActivity.class);
        startActivity(intent);
        finish();
    }
}
