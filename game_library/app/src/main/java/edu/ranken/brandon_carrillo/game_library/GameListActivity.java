package edu.ranken.brandon_carrillo.game_library;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.data.GameList;
import edu.ranken.brandon_carrillo.game_library.data.Platform;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameListAdapter;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameListViewModel;
import edu.ranken.brandon_carrillo.game_library.ui.utils.SpinnerOption;

public class GameListActivity extends AppCompatActivity {
    // constants
    private static final String LOG_TAG = GameListActivity.class.getSimpleName();

    // views
    private Spinner platformSpinner;
    private Spinner listSpinner;
    private TextView errorText;
    private RecyclerView recyclerView;

    // state
    private GameListViewModel model;
    private GameListAdapter gamesAdapter;
    private ArrayAdapter<SpinnerOption<String>> platformsAdapter;
    private ArrayAdapter<SpinnerOption> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_list);

        // find views
        platformSpinner = findViewById(R.id.platformSpinner);
        listSpinner = findViewById(R.id.listSpinner);
        errorText = findViewById(R.id.errorText);
        recyclerView = findViewById(R.id.gameList);

        // setup recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // setup view model & adapter
        model = new ViewModelProvider(this).get(GameListViewModel.class);
        gamesAdapter = new GameListAdapter(this, model);
        recyclerView.setAdapter(gamesAdapter);

        // populate list spinner
        SpinnerOption<GameList>[] listOptions = new SpinnerOption[] {
            new SpinnerOption<>(getString(R.string.allGames), GameList.ALL_GAMES),
            new SpinnerOption<>(getString(R.string.myLibrary), GameList.MY_LIBRARY),
            new SpinnerOption<>(getString(R.string.myWishlist), GameList.MY_WISHLIST)
        };

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listOptions);
        listSpinner.setAdapter(listAdapter);

        // observe model
        model.getGames().observe(this, (games) -> {
            gamesAdapter.setItems(games);
        });
        model.getLibraryValue().observe(this, (library) -> {
            gamesAdapter.setLibraryValue(library);
        });
        model.getWishlistValue().observe(this, (wishlist) -> {
            gamesAdapter.setWishlistValue(wishlist);
        });

        model.getPlatforms().observe(this, (platforms) -> {
            if (platforms != null) {
                int selectedPosition = 0;
                String selectedId = model.getFilterPlatformId();

                ArrayList<SpinnerOption<String>> platformNames = new ArrayList<>(platforms.size());
                platformNames.add(new SpinnerOption<>(getString(R.string.allPlatforms), null));

                for (int i = 0; i < platforms.size(); ++i) {
                    Platform platform = platforms.get(i);
                    if (platform.id != null && platform.name != null) {
                        platformNames.add(new SpinnerOption<>(platform.name, platform.id));
                        if (Objects.equals(platform.id, selectedId)) {
                            selectedPosition = platformNames.size() - 1;
                        }
                    }
                }
                platformsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, platformNames);
                platformSpinner.setAdapter(platformsAdapter);
                platformSpinner.setSelection(selectedPosition);
            }
        });

        model.getErrorMessage().observe(this, (errorMessage) -> {
            errorText.setVisibility(errorMessage != null ? View.VISIBLE : View.GONE);
            errorText.setText(errorMessage);
        });
        model.getSnackbarMessage().observe(this, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(recyclerView, snackbarMessage, Snackbar.LENGTH_LONG).show();
                model.clearSnackbar();
            }
        });

        // register listeners
        platformSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerOption<String> option = (SpinnerOption<String>) parent.getItemAtPosition(position);
                model.filterGamesByPlatform(option.getValue());
                Log.i(LOG_TAG, "Filter by platform: " + option.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerOption<GameList> option = (SpinnerOption<GameList>) parent.getItemAtPosition(position);
                model.filterGamesByList(option.getValue());
                Log.i(LOG_TAG, "Filter by list: " + option.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing.
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // force up navigation to have the same behavior as back navigation
            onBackPressed();
            return true;
        } else if (itemId == R.id.actionUserProfile) {
            onUserProfile();
            return true;
        } else if (itemId == R.id.actionSignOut) {
            onSignOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.i(LOG_TAG, "Back pressed");
    }

    public void onUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // FIXME: check if user is null (not fixed)
        String userId = user.getUid();
        if (user != null) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            intent.putExtra(MyProfileActivity.EXTRA_USER_ID, userId);
            this.startActivity(intent);
        }
    }

    public void onSignOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener((result) -> {
                Log.i(LOG_TAG, "Signed out.");
                finish();
        });
    }
}
