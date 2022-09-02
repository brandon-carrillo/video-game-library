package edu.ranken.brandon_carrillo.game_library.ui.user;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.GameDetailsActivity;
import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.GameWishlist;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameViewHolder;

public class MyWishlistAdapter extends RecyclerView.Adapter<GameViewHolder> {
    // constants
    private static final String LOG_TAG = MyWishlistAdapter.class.getSimpleName();

    private AppCompatActivity context;
    private LayoutInflater layoutInflater;
    private Picasso picasso;
    private MyProfileViewModel model;
    private List<GameWishlist> items;

    public MyWishlistAdapter(AppCompatActivity context, MyProfileViewModel model) {
        this.context = context;
        this.layoutInflater = layoutInflater.from(context);
        this.picasso = Picasso.get();
        this.model = model;
    }

    public void setItems(List<GameWishlist> items) {
        this.items = items;
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
        View itemView = layoutInflater.inflate(R.layout.item_profile_game, parent, false);

        model = new ViewModelProvider(context).get(MyProfileViewModel.class);

        GameViewHolder vh = new GameViewHolder(itemView);
        vh.name = itemView.findViewById(R.id.profileGameName);
        vh.developer = itemView.findViewById(R.id.profileGameDeveloper);
        vh.image = itemView.findViewById(R.id.profileGameIcon);

        vh.platformIcons = new ImageView[] {
            itemView.findViewById(R.id.profileGamePlatform1),
            itemView.findViewById(R.id.profileGamePlatform2),
            itemView.findViewById(R.id.profileGamePlatform3),
            itemView.findViewById(R.id.profileGamePlatform4)
        };

        vh.itemView.setOnClickListener((view) -> {
            GameWishlist game = items.get(vh.getAdapterPosition());
            Log.i(LOG_TAG, "Clicked on game: " + game.id);

            Intent intent = new Intent(context, GameDetailsActivity.class);
            intent.putExtra(GameDetailsActivity.EXTRA_GAME_ID, game.game.id);
            context.startActivity(intent);
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder vh, int position) {
        GameWishlist item = items.get(position);

        List<GameWishlist> wishlist = model.getWishlist().getValue();

        if (wishlist != null) {
            GameWishlist wish = wishlist.get(position);


            if (wish.selectedPlatforms == null) {
                for (int i = 0; i < vh.platformIcons.length; ++i) {
                    vh.platformIcons[i].setImageResource(0);
                    vh.platformIcons[i].setVisibility(View.GONE);
                }
            } else {
                int iconIndex = 0;
                for (Map.Entry<String, Boolean> entry : wish.selectedPlatforms.entrySet()) {
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

        }





        if (item.game.name == null || item.game.name.length() == 0) {
            vh.name.setText(R.string.nameMissing);
        } else if (item.game.releaseYear == null) {
            vh.name.setText(item.game.name);
        } else {
            vh.name.setText(context.getString(R.string.gameNameFormat, item.game.name, item.game.releaseYear));
        }

        if (item.game.developer == null || item.game.developer.length() == 0) {
            vh.developer.setText(R.string.developerMissing);
        } else {
            vh.developer.setText(item.game.developer);
        }

        Log.i(LOG_TAG, "icon:" + item.game.icon);

        if (item.game.icon == null || item.game.icon.length() == 0) {
            vh.image.setImageResource(R.drawable.ic_broken_image);
        } else {
            vh.image.setImageResource(R.drawable.ic_downloading);
            this.picasso
                .load(item.game.icon)
                .noPlaceholder()
                .error(R.drawable.ic_error)
                .resize(150, 150)
                .centerInside()
                .into(vh.image);
        }

    }
}
