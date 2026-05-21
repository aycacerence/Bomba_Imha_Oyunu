package com.example.codedefuse;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResultActivity extends Activity {

    private LinearLayout resultBox;
    private TextView titleText;
    private TextView messageText;
    private TextView remainingTimeText;
    private TextView mistakeText;
    private TextView solvedText;
    private TextView scoreText;
    private Button mainMenuButton;
    private Button playAgainButton;
    private Button scoreHistoryButton;
    private boolean savedToDb = false;
    private String resultText = "Kaybetti";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keepContentOutOfSystemBars();
        setContentView(R.layout.activity_result);

        if (savedInstanceState != null) {
            savedToDb = savedInstanceState.getBoolean("savedToDb", false);
        }

        connectViews();
        showResult();
        saveScoreToDatabase();
        setButtons();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("savedToDb", savedToDb);
    }

    private void connectViews() {
        resultBox = findViewById(R.id.resultBox);
        titleText = findViewById(R.id.txtResultTitle);
        messageText = findViewById(R.id.txtResultMessage);
        remainingTimeText = findViewById(R.id.txtResultTime);
        mistakeText = findViewById(R.id.txtResultMistakes);
        solvedText = findViewById(R.id.txtResultSolved);
        scoreText = findViewById(R.id.txtResultScore);
        mainMenuButton = findViewById(R.id.btnMainMenu);
        playAgainButton = findViewById(R.id.btnPlayAgain);
        scoreHistoryButton = findViewById(R.id.btnResultScoreHistory);
    }

    private void showResult() {
        String reason = getIntent().getStringExtra("reason");

        if (reason == null || reason.isEmpty()) {
            if (GameState.isGameWon) {
                reason = "success";
            } else if (GameState.mistakeCount >= GameState.maxMistakeCount) {
                reason = "mistakes";
            } else {
                reason = "time";
            }
        }

        if (reason.equals("success")) {
            resultText = "Kazandı";
            titleText.setText("Bomba İmha Edildi");
            messageText.setText("Tüm modüller başarıyla çözüldü.");
            resultBox.setBackgroundResource(R.drawable.module_solved);
            titleText.setTextColor(getResources().getColor(R.color.neon_green));
        } else if (reason.equals("mistakes")) {
            resultText = "Kaybetti";
            titleText.setText("Bomba Patladı");
            messageText.setText("Hata hakkı dolduğu için görev başarısız oldu.");
            resultBox.setBackgroundResource(R.drawable.module_unsolved);
            titleText.setTextColor(getResources().getColor(R.color.neon_red));
        } else {
            resultText = "Kaybetti";
            titleText.setText("Bomba Patladı");
            messageText.setText("Geri sayım tamamlandı ve görev başarısız oldu.");
            resultBox.setBackgroundResource(R.drawable.module_unsolved);
            titleText.setTextColor(getResources().getColor(R.color.neon_red));
        }

        remainingTimeText.setText("Kalan Süre: " + formatTime(GameState.remainingTime));
        mistakeText.setText("Hata Sayısı: " + GameState.mistakeCount + "/" + GameState.maxMistakeCount);
        solvedText.setText("Çözülen Modül: " + GameState.solvedModuleCount + "/" + GameState.totalModuleCount);
        scoreText.setText(String.valueOf(GameState.calculateScore()));
    }

    private void setButtons() {
        mainMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });

        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, DefuserSetupActivity.class);
            startActivity(intent);
            finish();
        });

        scoreHistoryButton.setOnClickListener(v -> openScoreHistory());
    }

    private void openScoreHistory() {
        Intent intent = new Intent(ResultActivity.this, ScoreHistoryActivity.class);
        startActivity(intent);
    }

    private void saveScoreToDatabase() {
        if (savedToDb) {
            return;
        }

        savedToDb = true;

        String username = GameState.username;
        if (username == null || username.trim().isEmpty()) {
            username = "Oyuncu";
        }

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date());
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        databaseHelper.insertScore(
                username,
                GameState.calculateScore(),
                resultText,
                GameState.remainingTime,
                GameState.mistakeCount,
                GameState.solvedModuleCount,
                GameState.totalModuleCount,
                date
        );
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void keepContentOutOfSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }
    }
}
