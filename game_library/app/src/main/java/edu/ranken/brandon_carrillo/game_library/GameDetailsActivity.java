package edu.ranken.brandon_carrillo.game_library;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.data.Game;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameDetailsFragment;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameDetailsViewModel;
import edu.ranken.brandon_carrillo.game_library.ui.review.ReviewListAdapter;

public class GameDetailsActivity extends BaseActivity {
    // constants
    private static final String LOG_TAG = GameDetailsActivity.class.getName();
    public static final String EXTRA_GAME_ID = "gameId";

    // views
    private FragmentContainerView fragmentContainer;
    private GameDetailsFragment fragment;
    private GameDetailsViewModel model;

    // state
    private String gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        fragmentContainer = findViewById(R.id.fragmentContainer);
        fragment = new GameDetailsFragment();
        model = new ViewModelProvider(this).get(GameDetailsViewModel.class);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();



        if (savedInstanceState == null) {
            // get intent
            Intent intent = getIntent();
            String intentAction = intent.getAction();
            Uri intentData = intent.getData();
            if (intentAction == null) {
                gameId = intent.getStringExtra(EXTRA_GAME_ID);
                model.fetchGame(gameId);
            } else if (Objects.equals(intentAction, Intent.ACTION_VIEW) && intentData != null) {
                handleWebLink(intent);
            }
        } else {
            Log.i(LOG_TAG, "gameId: " + gameId);
            gameId = savedInstanceState.getString("gameId");
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("gameId", gameId);
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


    @Override
    public void onBackPressed() {
        if (getIntent().getAction() != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(new Intent(this, LoginActivity.class));
            stackBuilder.addNextIntent(new Intent(this, HomeActivity.class));
            stackBuilder.startActivities();
        } else {
            super.onBackPressed();
        }
    }


    private void handleWebLink(Intent intent) {
        Uri uri = intent.getData();
        String path = uri.getPath();
        String prefix = "/game/";

        // parse uri path
        if (path.startsWith(prefix)) {
            int gameIdEnd = path.indexOf("/", prefix.length());

            if (gameIdEnd < 0) {
                gameId = path.substring(prefix.length());
            } else {
                gameId = path.substring(prefix.length(), gameIdEnd);
            }

        } else {
            gameId = null;
        }


        // load game data
        model.fetchGame(gameId);
    }



}