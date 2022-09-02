package edu.ranken.brandon_carrillo.game_library.ui.game;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.GameDetailsActivity;
import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.GameLibraryValue;
import edu.ranken.brandon_carrillo.game_library.data.GameSummary;
import edu.ranken.brandon_carrillo.game_library.data.GameWishlistValue;
import edu.ranken.brandon_carrillo.game_library.ui.PlatformChooserDialog;

public class GameListAdapter extends RecyclerView.Adapter<GameViewHolder> {
    // constants
    private static final String LOG_TAG = GameListAdapter.class.getSimpleName();

    private final FragmentActivity context;
    private final LayoutInflater layoutInflater;
    private final Picasso picasso;
    private final GameListViewModel model;
    private List<GameSummary> items;
    private List<GameLibraryValue> libraryValue;
    private List<GameWishlistValue> wishlistValue;

//    private GameSummary game;

    public GameListAdapter(FragmentActivity context, GameListViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.picasso = Picasso.get();
        this.model = model;
    }

    public void setItems(List<GameSummary> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setLibraryValue(List<GameLibraryValue> libraryValue) {
        this.libraryValue = libraryValue;
        notifyDataSetChanged();
    }

    public void setWishlistValue(List<GameWishlistValue> wishlistValue) {
        this.wishlistValue = wishlistValue;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (items != null) {
            return items.size();
        }
        return 0;
    }



    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.game_item, parent, false);

        GameViewHolder vh = new GameViewHolder(itemView);
        vh.name = itemView.findViewById(R.id.item_gameProfile_name);
        vh.developer = itemView.findViewById(R.id.item_gameProfile_developer);
        vh.image = itemView.findViewById(R.id.item_gameProfile_image);
        vh.library = itemView.findViewById(R.id.item_game_library);
        vh.wishlist = itemView.findViewById(R.id.item_game_wishlist);

        vh.platformIcons = new ImageView[] {
            itemView.findViewById(R.id.item_game_platform1),
            itemView.findViewById(R.id.item_game_platform2),
            itemView.findViewById(R.id.item_game_platform3),
            itemView.findViewById(R.id.item_game_platform4)
        };

        vh.library.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "on click library.");


            GameSummary game = items.get(vh.getAdapterPosition());
            Log.i(LOG_TAG, "platforms: " + game.supportedPlatforms);


            PlatformChooserDialog dialog = new PlatformChooserDialog(
                context,
                R.string.choosePlatforms,
                game.supportedPlatforms,
                null,
                selectedPlatforms -> {
//                        model.addToLibrary(game, selectedPlatforms);


                        if (vh.libraryValue) {
                            model.removeFromLibrary(game.id, true);
                        } else {
                            if (vh.wishlistValue) {
                                model.removeFromWishlist(game.id, false);
                            }
                            model.addToLibrary(game, selectedPlatforms);
                        }



                }
            );
            dialog.show();
        });

        vh.wishlist.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "on click wishlist." + model);


            GameSummary game = items.get(vh.getAdapterPosition());


            PlatformChooserDialog dialog = new PlatformChooserDialog(
                context,
                R.string.choosePlatforms,
                game.supportedPlatforms,
                null,
                selectedPlatforms -> {


                    if (vh.wishlistValue) {
                        model.removeFromWishlist(game.id, true);
                    } else {
                        if (vh.libraryValue) {
                            model.removeFromLibrary(game.id, false);
                        }
                        model.addToWishlist(game, selectedPlatforms);
                    }

                }
            );
            dialog.show();

        });

        vh.itemView.setOnClickListener((view) -> {
            GameSummary game = items.get(vh.getAdapterPosition());
            Log.i(LOG_TAG, "Clicked on game: " + game.id);

            model.setSelectedGame(game);

        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder vh, int position) {
        GameSummary item = items.get(position);
//        game = item;

        if (item.supportedPlatforms == null) {
            for (int i = 0; i < vh.platformIcons.length; ++i) {
                vh.platformIcons[i].setImageResource(0);
                vh.platformIcons[i].setVisibility(View.GONE);
            }
        } else {
            int iconIndex = 0;
            for (Map.Entry<String, Boolean> entry : item.supportedPlatforms.entrySet()) {
                if (Objects.equals(entry.getValue(), Boolean.TRUE)) {
                    switch (entry.getKey()) {
                        default:
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_error);
                            vh.platformIcons[iconIndex].setContentDescription(context.getString(R.string.unknownPlatform));
                            break;
                        case "playstation":
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_playstation);
                            vh.platformIcons[iconIndex].setContentDescription(context.getString(R.string.playStation));
                            break;
                        case "xbox":
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_xbox);
                            vh.platformIcons[iconIndex].setContentDescription(context.getString(R.string.xbox));
                            break;
                        case "windows":
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_windows);
                            vh.platformIcons[iconIndex].setContentDescription(context.getString(R.string.windows));
                            break;
                        case "switch":
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_switch);
                            vh.platformIcons[iconIndex].setContentDescription(context.getString(R.string.nSwitch));
                            break;
                    }
                    iconIndex++;
                    if (iconIndex >= vh.platformIcons.length) {
                        break;
                    }
                }
            }
            for (; iconIndex < vh.platformIcons.length; ++iconIndex) {
                vh.platformIcons[iconIndex].setImageResource(0);
                vh.platformIcons[iconIndex].setVisibility(View.GONE);
            }
        }

        if (item.name == null || item.name.length() == 0) {
            vh.name.setText(R.string.nameMissing);
        } else if (item.releaseYear == null) {
            vh.name.setText(item.name);
        } else {
            vh.name.setText(context.getString(R.string.gameNameFormat, item.name, item.releaseYear));
        }

        if (item.developer == null || item.developer.length() == 0) {
            vh.developer.setText(R.string.developerMissing);
        } else {
            vh.developer.setText(item.developer);
        }

        if (item.image == null || item.image.length() == 0) {
            vh.image.setImageResource(R.drawable.ic_broken_image);
        } else {
            vh.image.setImageResource(R.drawable.ic_downloading);
            this.picasso
                .load(item.image)
                .noPlaceholder()
                .error(R.drawable.ic_error)
                 .resizeDimen(R.dimen.picassoGameThumbnailWidth, R.dimen.picassoGameThumbnailHeight)
                .centerInside()
                .into(vh.image);
        }

        vh.library.setImageResource(R.drawable.ic_library_outline);
        vh.library.setVisibility(model != null && libraryValue != null ? View.VISIBLE : View.GONE);
        vh.libraryValue = false;

        if (libraryValue != null) {
            for (GameLibraryValue lib : libraryValue) {
                if (Objects.equals(item.id, lib.gameId)) {
                    vh.libraryValue = lib.value;
                    if (lib.value == true) {
                        vh.library.setImageResource(R.drawable.ic_library_solid);
                    } else {
                        vh.library.setImageResource(R.drawable.ic_library_outline);
                    }
                    break;
                }
            }
        }

        vh.wishlist.setImageResource(R.drawable.ic_wishlist_outline);
        vh.wishlist.setVisibility(model != null && wishlistValue != null ? View.VISIBLE : View.GONE);
        vh.wishlistValue = false;

        if (wishlistValue != null) {
            for (GameWishlistValue wish : wishlistValue) {
                if (Objects.equals(item.id, wish.gameId)) {
                    vh.wishlistValue = wish.value;
                    if (wish.value == true) {
                        vh.wishlist.setImageResource(R.drawable.ic_wishlist_solid);
                    } else {
                        vh.wishlist.setImageResource(R.drawable.ic_wishlist_outline);
                    }
                    break;
                }
            }
        }
    }
}
