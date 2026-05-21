package com.example.codedefuse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ExpertMenuActivity extends Activity {

    private LinearLayout wireGuideCard;
    private LinearLayout buttonGuideCard;
    private LinearLayout colorMemoryGuideCard;
    private LinearLayout dialGuideCard;
    private LinearLayout passwordGuideCard;
    private LinearLayout backToRolesCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_menu);

        wireGuideCard = findViewById(R.id.cardWireGuide);
        buttonGuideCard = findViewById(R.id.cardButtonGuide);
        colorMemoryGuideCard = findViewById(R.id.cardColorMemoryGuide);
        dialGuideCard = findViewById(R.id.cardDialGuide);
        passwordGuideCard = findViewById(R.id.cardPasswordGuide);
        backToRolesCard = findViewById(R.id.cardBackToRoles);

        wireGuideCard.setOnClickListener(v -> openActivity(WireGuideActivity.class));
        buttonGuideCard.setOnClickListener(v -> openActivity(ButtonGuideActivity.class));
        colorMemoryGuideCard.setOnClickListener(v -> openActivity(ColorMemoryGuideActivity.class));
        dialGuideCard.setOnClickListener(v -> openActivity(DialGuideActivity.class));
        passwordGuideCard.setOnClickListener(v -> openActivity(PasswordGuideActivity.class));

        backToRolesCard.setOnClickListener(v -> {
            Intent intent = new Intent(ExpertMenuActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
}
