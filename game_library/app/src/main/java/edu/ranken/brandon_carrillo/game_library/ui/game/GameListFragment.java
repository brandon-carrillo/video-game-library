package edu.ranken.brandon_carrillo.game_library.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.GameList;
import edu.ranken.brandon_carrillo.game_library.data.Platform;
import edu.ranken.brandon_carrillo.game_library.ui.utils.SpinnerOption;

public class GameListFragment extends Fragment {
    // constants
    private static final String LOG_TAG = GameListFragment.class.getSimpleName();

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

    public GameListFragment() {
        super(R.layout.game_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // find views
        platformSpinner = view.findViewById(R.id.platformSpinner);
        listSpinner = view.findViewById(R.id.listSpinner);
        errorText = view.findViewById(R.id.errorText);
        recyclerView = view.findViewById(R.id.gameList);

        // get activity
        FragmentActivity activity = getActivity();
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        // setup recycler view
//        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        int columns = getResources().getInteger(R.integer.gameListColumns);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, columns));

        // setup view model & adapter
        model = new ViewModelProvider(activity).get(GameListViewModel.class);
        gamesAdapter = new GameListAdapter(activity, model);
        recyclerView.setAdapter(gamesAdapter);

        // populate list spinner
        SpinnerOption<GameList>[] listOptions = new SpinnerOption[] {
            new SpinnerOption<>(getString(R.string.allGames), GameList.ALL_GAMES),
            new SpinnerOption<>(getString(R.string.myLibrary), GameList.MY_LIBRARY),
            new SpinnerOption<>(getString(R.string.myWishlist), GameList.MY_WISHLIST)
        };

        listAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, listOptions);
        listSpinner.setAdapter(listAdapter);


        // observe model
        model.getGames().observe(lifecycleOwner, (games) -> {
            gamesAdapter.setItems(games);
        });
        model.getLibraryValue().observe(lifecycleOwner, (library) -> {
            gamesAdapter.setLibraryValue(library);
        });
        model.getWishlistValue().observe(lifecycleOwner, (wishlist) -> {
            gamesAdapter.setWishlistValue(wishlist);
        });

        model.getPlatforms().observe(lifecycleOwner, (platforms) -> {
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
                platformsAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, platformNames);
                platformSpinner.setAdapter(platformsAdapter);
                platformSpinner.setSelection(selectedPosition);
            }
        });

        model.getErrorMessage().observe(lifecycleOwner, (errorMessage) -> {
            errorText.setVisibility(errorMessage != null ? View.VISIBLE : View.GONE);
            if (errorMessage != null) {
                errorText.setText(errorMessage);
            }
        });
        model.getSnackbarMessage().observe(lifecycleOwner, (snackbarMessage) -> {
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


}
