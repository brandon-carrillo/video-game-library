package edu.ranken.brandon_carrillo.game_library;


import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;


import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.ui.review.ComposeReviewViewModel;

public class ComposeReviewActivity extends BaseActivity {
    // constants
    private static final String LOG_TAG = ComposeReviewActivity.class.getName();
    public static final String EXTRA_GAME_ID = "gameId";

    // state
    private String gameId;
    private ComposeReviewViewModel model;

    private TextView reviewGameTitleText;
    private EditText reviewEditText;
    private Button publishReviewButton;
    private TextView reviewErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_review);

        // find views
        reviewGameTitleText = findViewById(R.id.reviewGameTitleText);
        reviewEditText = findViewById(R.id.reviewEditText);
        publishReviewButton = findViewById(R.id.publishReviewButton);
        reviewErrorText = findViewById(R.id.reviewErrorText);

        // get intent
        Intent intent = getIntent();
        gameId = intent.getStringExtra(EXTRA_GAME_ID);

        // bind model
        model = new ViewModelProvider(this).get(ComposeReviewViewModel.class);
        model.fetchGame(gameId);
        model.getGameName().observe(this, (gameName) -> {
            if (gameName == null) {
                reviewGameTitleText.setText(null);
            } else {
                reviewGameTitleText.setText(gameName);
            }
        });
        model.getErrorMessage().observe(this, (errorMessage) -> {
            reviewErrorText.setVisibility(errorMessage != null ? View.VISIBLE : View.GONE);
            if (errorMessage != null) {
                reviewErrorText.setText(errorMessage);
            }
        });
        model.getSnackbarMessage().observe(this, (message) -> {
            if (message != null) {
                Snackbar.make(reviewGameTitleText, message, Snackbar.LENGTH_SHORT).show();
                model.clearSnackbar();
            }
        });
        model.getFinished().observe(this, (finished) -> {
            if (Objects.equals(finished, Boolean.TRUE)) {
                finish();
            }
        });

        // register listeners
        publishReviewButton.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "Publish review clicked.");
            model.publishReview(gameId, reviewEditText.getText().toString());
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // force up navigation to have the same behavior as back navigation
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}