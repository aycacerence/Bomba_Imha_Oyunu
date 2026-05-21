package com.example.codedefuse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ScoreHistoryActivity extends Activity {

    private ListView scoreListView;
    private TextView emptyText;
    private Button clearButton;
    private Button mainMenuButton;

    private DatabaseHelper databaseHelper;
    private ArrayList<Score> scores;
    private ScoreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);

        scoreListView = findViewById(R.id.listScores);
        emptyText = findViewById(R.id.txtEmptyScores);
        clearButton = findViewById(R.id.btnClearScores);
        mainMenuButton = findViewById(R.id.btnScoreMainMenu);

        databaseHelper = new DatabaseHelper(this);
        loadScores();

        clearButton.setOnClickListener(v -> {
            databaseHelper.clearScores();
            Toast.makeText(this, "Skor geçmişi temizlendi.", Toast.LENGTH_SHORT).show();
            loadScores();
        });

        mainMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(ScoreHistoryActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadScores() {
        scores = databaseHelper.getAllScores();
        adapter = new ScoreAdapter(this, scores);
        scoreListView.setAdapter(adapter);

        if (scores.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            scoreListView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            scoreListView.setVisibility(View.VISIBLE);
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private class ScoreAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<Score> scoreItems;

        ScoreAdapter(Context context, ArrayList<Score> scoreItems) {
            this.context = context;
            this.scoreItems = scoreItems;
        }

        @Override
        public int getCount() {
            return scoreItems.size();
        }

        @Override
        public Object getItem(int position) {
            return scoreItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return scoreItems.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_score, parent, false);
            }

            Score score = scoreItems.get(position);

            TextView usernameText = convertView.findViewById(R.id.txtItemUsername);
            TextView resultText = convertView.findViewById(R.id.txtItemResult);
            TextView scoreText = convertView.findViewById(R.id.txtItemScore);
            TextView detailText = convertView.findViewById(R.id.txtItemDetails);
            TextView dateText = convertView.findViewById(R.id.txtItemDate);

            usernameText.setText(score.getUsername());
            resultText.setText(score.getResult());
            scoreText.setText("Skor: " + score.getScore());
            detailText.setText("Süre: " + formatTime(score.getRemainingTime()) +
                    "   Hata: " + score.getMistakeCount() +
                    "   Modül: " + score.getSolvedModuleCount() + "/" + score.getTotalModuleCount());
            dateText.setText(score.getDate());

            return convertView;
        }
    }
}
