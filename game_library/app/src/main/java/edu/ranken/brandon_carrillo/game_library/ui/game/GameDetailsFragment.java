package edu.ranken.brandon_carrillo.game_library.ui.game;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.ComposeReviewActivity;
import edu.ranken.brandon_carrillo.game_library.EbayBrowseActivity;
import edu.ranken.brandon_carrillo.game_library.GameDetailsActivity;
import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.Game;
import edu.ranken.brandon_carrillo.game_library.ui.review.ReviewListAdapter;

public class GameDetailsFragment extends Fragment {
    // constants
    private static final String LOG_TAG = GameDetailsFragment.class.getSimpleName();

    // state
    private String gameId;
    private Game game;
    private GameDetailsViewModel model;
    private ReviewListAdapter reviewsAdapter;

    private TextView gameErrorText;
    private ImageView gameBanner;
    private TextView gameTitleText;
    private TextView gameDescriptionText;
    private TextView controllerSupportText;
    private TextView multiplayerSupportText;
    private TextView genreText;
    private TextView tagsText;
    private ImageView[] platformIcons;
    private Button ebayButton;
    private ImageView screenshot1;
    private ImageView screenshot2;
    private ImageView screenshot3;
    private FloatingActionButton composeReviewButton;
    private FloatingActionButton shareGameButton;
    private TextView gameReviewsErrorText;
    private RecyclerView recyclerView;
    private TextView ebayAvgText;

    public GameDetailsFragment() {
        super(R.layout.game_details_scroll);
    }

    @Override
    public void onViewCreated(@NonNull View contentView, @Nullable Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onViewCreated");
        super.onViewCreated(contentView, savedInstanceState);

        // find views
        gameErrorText = contentView.findViewById(R.id.gameErrorText);
        gameBanner = contentView.findViewById(R.id.gameBanner);
        gameTitleText = contentView.findViewById(R.id.gameTitleText);
        gameDescriptionText = contentView.findViewById(R.id.gameDescriptionText);
        controllerSupportText = contentView.findViewById(R.id.controllerSupportText);
        multiplayerSupportText = contentView.findViewById(R.id.multiplayerSupportText);
        genreText = contentView.findViewById(R.id.genreText);
        tagsText = contentView.findViewById(R.id.tagsText);
        platformIcons = new ImageView[] {
            contentView.findViewById(R.id.gamePlatform1),
            contentView.findViewById(R.id.gamePlatform2),
            contentView.findViewById(R.id.gamePlatform3),
            contentView.findViewById(R.id.gamePlatform4)
        };
        ebayButton = contentView.findViewById(R.id.ebayButton);
        screenshot1 = contentView.findViewById(R.id.gameScreenshot1);
        screenshot2 = contentView.findViewById(R.id.gameScreenshot2);
        screenshot3 = contentView.findViewById(R.id.gameScreenshot3);
        composeReviewButton = contentView.findViewById(R.id.composeReviewButton);
        shareGameButton = contentView.findViewById(R.id.shareGameButton);
        gameReviewsErrorText = contentView.findViewById(R.id.gameReviewsErrorText);
        recyclerView = contentView.findViewById(R.id.reviewList);
        ebayAvgText = contentView.findViewById(R.id.ebayAvgText);


        // setup recycler view
        FragmentActivity activity = getActivity();
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));

        model = new ViewModelProvider(activity).get(GameDetailsViewModel.class);

        reviewsAdapter = new ReviewListAdapter(activity, model);
        recyclerView.setAdapter(reviewsAdapter);

        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        model.getGame().observe(lifecycleOwner, (game) -> {
            this.gameId = model.getGameId();
            this.game = game;

            if (game == null) {
                gameTitleText.setText(null);
                gameDescriptionText.setText(null);
            } else {

                if (game.name != null && game.ebay.get("keywords") != null && game.ebay.get("minPrice") != null && game.ebay.get("maxPrice") != null) {
                    String name = game.name;
                    String keywords = game.ebay.get("keywords");
                    String query = name + " " + keywords;
                    String minPrice = game.ebay.get("minPrice");
                    String maxPrice = game.ebay.get("maxPrice");
                    String filterByPrice = "price:[" + minPrice + ".." + maxPrice + "],priceCurrency:USD";

                    model.queryGame(query, filterByPrice, "");
                }

                if (game.banner == null) {
                    gameBanner.setImageResource(R.drawable.ic_error);
                } else {
                    gameBanner.setImageResource(R.drawable.ic_downloading);
                    Picasso.get().load(game.banner).noPlaceholder().error(R.drawable.ic_error)
                        .resize(1280, 720).centerInside().into(gameBanner);
                }

                if (game.name == null || game.name.length() == 0) {
                    gameTitleText.setText(R.string.nameMissing);
                } else if (game.releaseYear == null) {
                    gameTitleText.setText(game.name);
                } else {
                    gameTitleText.setText(this.getString(R.string.gameNameFormat, game.name, game.releaseYear));
                }

                if (game.description == null) {
                    gameDescriptionText.setText(R.string.noDescription);
                } else {
                    gameDescriptionText.setText(game.description);
                }
                if (game.controllerSupport == null) {
                    controllerSupportText.setText("");
                } else {
                    controllerSupportText.setText(game.controllerSupport);
                }
                if (game.multiplayerSupport == null) {
                    multiplayerSupportText.setText("");
                } else {
                    multiplayerSupportText.setText(game.multiplayerSupport);
                }
                if (game.genre == null) {
                    genreText.setText("");
                } else {
                    genreText.setText(game.genre);
                }
                if (game.tags == null) {
                    tagsText.setText("");
                } else {
                    tagsText.setText(game.tags);
                }
                if (game.supportedPlatforms == null) {
                    for (int i = 0; i < platformIcons.length; ++i) {
                        platformIcons[i].setImageResource(0);
                        platformIcons[i].setVisibility(View.GONE);
                    }
                } else {
                    int iconIndex = 0;
                    for (Map.Entry<String, Boolean> entry : game.supportedPlatforms.entrySet()) {
                        if (Objects.equals(entry.getValue(), Boolean.TRUE)) {
                            switch (entry.getKey()) {
                                default:
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_error);
                                    break;
                                case "playstation":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_playstation);
                                    break;
                                case "xbox":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_xbox);
                                    break;
                                case "windows":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_windows);
                                    break;
                                case "switch":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_switch);
                                    break;
                            }
                            iconIndex++;
                            if (iconIndex >= platformIcons.length) {
                                break;
                            }
                        }
                    }
                    for (; iconIndex < platformIcons.length; ++iconIndex) {
                        platformIcons[iconIndex].setImageResource(0);
                        platformIcons[iconIndex].setVisibility(View.GONE);
                    }
                }
                if (game.screenshot1 == null) {
                    screenshot1.setImageResource(R.drawable.ic_error);
                } else {
                    screenshot1.setImageResource(R.drawable.ic_downloading);
                    Picasso.get().load(game.screenshot1).noPlaceholder().error(R.drawable.ic_error)
                        .resize(1280, 720).centerInside().into(screenshot1);
                }
                if (game.screenshot2 == null) {
                    screenshot2.setImageResource(R.drawable.ic_error);
                } else {
                    screenshot2.setImageResource(R.drawable.ic_downloading);
                    Picasso.get().load(game.screenshot2).noPlaceholder().error(R.drawable.ic_error)
                        .resize(1280, 720).centerInside().into(screenshot2);
                }
                if (game.screenshot3 == null) {
                    screenshot3.setImageResource(R.drawable.ic_error);
                } else {
                    screenshot3.setImageResource(R.drawable.ic_downloading);
                    Picasso.get().load(game.screenshot3).noPlaceholder().error(R.drawable.ic_error)
                        .resize(1280, 720).centerInside().into(screenshot3);
                }
            }
        });
        model.getAvg().observe(lifecycleOwner, (avg) -> {
            if (avg != null) {
                String format = String.format("%.2f", avg);
                String text = getString(R.string.averagePriceUSD) + format;
                ebayAvgText.setText(text);
            }
        });
        model.getGameError().observe(lifecycleOwner, (gameError) -> {
            gameErrorText.setVisibility(gameError != null ? View.VISIBLE : View.GONE);
            if (gameError != null) {
                gameErrorText.setText(gameError);
            }
        });
        model.getSnackbarMessage().observe(lifecycleOwner, (message) -> {
            if (message != null) {
                Snackbar.make(gameTitleText, message, Snackbar.LENGTH_SHORT).show();
                model.clearSnackbar();
            }
        });
        model.getReviews().observe(lifecycleOwner, (reviews) -> {
            reviewsAdapter.setItems(reviews);
        });
        model.getGameReviewsError().observe(lifecycleOwner, (error) -> {
            gameReviewsErrorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
            if (error != null) {
                gameReviewsErrorText.setText(error);
            }
        });

        // register listeners
        ebayButton.setOnClickListener((v) -> {
            Log.i(LOG_TAG, "Find on eBay clicked.");

            Intent newIntent = new Intent(activity, EbayBrowseActivity.class);
            newIntent.putExtra(EbayBrowseActivity.EXTRA_GAME_ID, gameId);
            this.startActivity(newIntent);
        });

        composeReviewButton.setOnClickListener((v) -> {
            Log.i(LOG_TAG, "Compose review clicked.");

            Intent newIntent = new Intent(activity, ComposeReviewActivity.class);
            newIntent.putExtra(ComposeReviewActivity.EXTRA_GAME_ID, gameId);
            this.startActivity(newIntent);
        });

        shareGameButton.setOnClickListener((v) -> {
            Log.i(LOG_TAG, "Share game clicked.");
            String gameName;

            if (game == null) {
                Snackbar.make(v, "No Game found.", Snackbar.LENGTH_SHORT).show();
            } else if (game.name == null) {
                Snackbar.make(v, "Game does not have a name.", Snackbar.LENGTH_SHORT).show();
            } else {
                if (game.releaseYear == null) {
                    gameName = game.name;
                } else {
                    gameName = getString(R.string.gameNameFormat, game.name, game.releaseYear);
                }

                String message =
                    getString(R.string.shareGameMessage) + "\n" +
                        gameName +
                        "\nhttps://my-game-list.com/game/" + game.id;


                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                sendIntent.putExtra(Intent.EXTRA_TITLE, gameName);
                sendIntent.setType("text/plain");

                startActivity(Intent.createChooser(sendIntent, gameName));
            }
        });



    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("gameId", gameId);

    }


}
