package edu.ranken.brandon_carrillo.game_library;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.data.Game;
import edu.ranken.brandon_carrillo.game_library.ui.ebay.EbayBrowseViewModel;
import edu.ranken.brandon_carrillo.game_library.ui.ebay.EbayItemListAdapter;
import edu.ranken.brandon_carrillo.game_library.ui.utils.SpinnerOption;

public class EbayBrowseActivity extends AppCompatActivity {
    // constants
    private static final String LOG_TAG = EbayBrowseActivity.class.getName();
    public static final String EXTRA_GAME_ID = "gameId";

    // views
    private TextView ebayGameErrorText;
    private Spinner ebayPlatformSpinner;
    private Switch priceSwitch;
    private TextView ebayResultsErrorText;
    private RecyclerView recyclerView;

    // state
    private String gameId;
    private Game game;
    private String query;
    private String filterByPrice;
    private EbayBrowseViewModel model;
    private EbayItemListAdapter adapter;
    private ArrayAdapter<SpinnerOption<String>> platformsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ebay_browse);

        // find views
        ebayGameErrorText = findViewById(R.id.ebayGameErrorText);
        ebayPlatformSpinner = findViewById(R.id.ebayPlatformSpinner);
        priceSwitch = findViewById(R.id.priceSwitch);
        ebayResultsErrorText = findViewById(R.id.ebayResultsErrorText);
        recyclerView = findViewById(R.id.ebayList);

        // get intent
        Intent intent = getIntent();
        gameId = intent.getStringExtra(EXTRA_GAME_ID);

        // setup recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // setup view model & adapter
        model = new ViewModelProvider(this).get(EbayBrowseViewModel.class);
        adapter = new EbayItemListAdapter(this, model);
        recyclerView.setAdapter(adapter);

        // observe model
        model.fetchGame(gameId);
        model.getGame().observe(this, (game) -> {
            if (game == null) {

            } else {
                this.game = game;

                String name = game.name;
                String keywords = game.ebay.get("keywords");
                query = name + " " + keywords;
                String minPrice = game.ebay.get("minPrice");
                String maxPrice = game.ebay.get("maxPrice");
                filterByPrice = "price:[" + minPrice + ".." + maxPrice + "],priceCurrency:USD";

                model.queryGame(query, filterByPrice, "");




                ArrayList<SpinnerOption<String>> platformNames = new ArrayList<>(game.supportedPlatforms.size());
                if (game.supportedPlatforms.size() != 1) {
                    platformNames.add(new SpinnerOption<>(getString(R.string.allPlatforms), ""));
                }

                for (Map.Entry<String, Boolean> entry : game.supportedPlatforms.entrySet()) {
                    if (Objects.equals(entry.getValue(), Boolean.TRUE)) {

                        if (entry.getKey().equals("playstation")) {
                            platformNames.add(new SpinnerOption<>("PlayStation", "PlayStation"));
                        }
                        if (entry.getKey().equals("xbox")) {
                            platformNames.add(new SpinnerOption<>("Xbox", "Xbox"));
                        }
                        if (entry.getKey().equals("windows")) {
                            platformNames.add(new SpinnerOption<>("Windows", "Windows"));
                        }
                        if (entry.getKey().equals("switch")) {
                            platformNames.add(new SpinnerOption<>("Switch", "Switch"));
                        }

                    }
                }



                platformsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, platformNames);
                ebayPlatformSpinner.setAdapter(platformsAdapter);




            }
        });
        model.getItems().observe(this, (items) -> {
                adapter.setItems(items);
        });
        model.getGameError().observe(this, (error) -> {
            ebayGameErrorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                ebayGameErrorText.setText(error);
            }
        });
        model.getResultsError().observe(this, (error) -> {
            ebayResultsErrorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                ebayResultsErrorText.setText(error);
            }
        });




        // register listeners
        ebayPlatformSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerOption<String> option = (SpinnerOption<String>) parent.getItemAtPosition(position);
                Log.i(LOG_TAG, "Filter by platform: " + option.getValue());

                String name = game.name;
                String platform = option.getValue();
                String keywords = game.ebay.get("keywords");
                query = name + " " + platform + " " + keywords;
                String minPrice = game.ebay.get("minPrice");
                String maxPrice = game.ebay.get("maxPrice");
                filterByPrice = "price:[" + minPrice + ".." + maxPrice + "],priceCurrency:USD";
                model.queryGame(query, filterByPrice, "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });



        priceSwitch.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "checked: " + priceSwitch.isChecked());
            if (priceSwitch.isChecked()) {
                model.queryGame(query, filterByPrice, "price");
            } else {
                model.queryGame(query, filterByPrice, "");
            }
        });






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