package com.example.codedefuse;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class RoleSelectionActivity extends Activity {

    private Button defuserButton;
    private Button expertButton;
    private Button scoreButton;
    private Button homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keepContentOutOfSystemBars();
        setContentView(R.layout.activity_role_selection);

        defuserButton = findViewById(R.id.btnDefuserMode);
        expertButton = findViewById(R.id.btnExpertMode);
        scoreButton = findViewById(R.id.btnScoreHistory);
        homeButton = findViewById(R.id.btnBackHome);

        defuserButton.setOnClickListener(v -> openActivity(DefuserSetupActivity.class));
        expertButton.setOnClickListener(v -> openActivity(ExpertMenuActivity.class));
        scoreButton.setOnClickListener(v -> openActivity(ScoreHistoryActivity.class));

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        });

        centerContentWhenItFits();
    }

    private void centerContentWhenItFits() {
        ScrollView scrollView = findViewById(R.id.roleScrollView);
        LinearLayout contentContainer = findViewById(R.id.roleContentContainer);

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (scrollView.getHeight() == 0 || contentContainer.getChildCount() == 0) {
                    return;
                }

                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            int contentHeight = getVisibleChildrenHeight(contentContainer);
            int centeredPadding = (scrollView.getHeight() - contentHeight) / 2;
            int verticalPadding = Math.max(dp(18), centeredPadding);

            contentContainer.setPadding(
                    contentContainer.getPaddingLeft(),
                    verticalPadding,
                    contentContainer.getPaddingRight(),
                    verticalPadding
            );
            }
        });
    }

    private int getVisibleChildrenHeight(LinearLayout contentContainer) {
        int firstTop = Integer.MAX_VALUE;
        int lastBottom = 0;

        for (int i = 0; i < contentContainer.getChildCount(); i++) {
            View child = contentContainer.getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            firstTop = Math.min(firstTop, child.getTop());
            lastBottom = Math.max(lastBottom, child.getBottom());
        }

        if (firstTop == Integer.MAX_VALUE) {
            return 0;
        }

        return lastBottom - firstTop;
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    private void keepContentOutOfSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
