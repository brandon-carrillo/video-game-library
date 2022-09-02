package edu.ranken.brandon_carrillo.game_library.ui.user;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.GameDetailsActivity;
import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.GameLibrary;
import edu.ranken.brandon_carrillo.game_library.data.GameSummary;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameViewHolder;

public class ProfileAdapter extends RecyclerView.Adapter<GameViewHolder> {
    // constants
    private static final String LOG_TAG = ProfileAdapter.class.getSimpleName();

    private AppCompatActivity context;
    private LayoutInflater layoutInflater;
    private Picasso picasso;
    private MyProfileViewModel model;
    private List<GameSummary> items;

    public ProfileAdapter(AppCompatActivity context, MyProfileViewModel model) {
        this.context = context;
        this.layoutInflater = layoutInflater.from(context);
        this.picasso = Picasso.get();
        this.model = model;
    }

    public void setItems(List<GameSummary> items) {
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
            GameSummary game = items.get(vh.getAdapterPosition());
            Log.i(LOG_TAG, "Clicked on game: " + game.id);

            Intent intent = new Intent(context, GameDetailsActivity.class);
            intent.putExtra(GameDetailsActivity.EXTRA_GAME_ID, game.id);
            context.startActivity(intent);
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder vh, int position) {
        GameSummary item = items.get(position);


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
                            break;
                        case "playstation":
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_playstation);
                            break;
                        case "xbox":
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_xbox);
                            break;
                        case "windows":
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_windows);
                            break;
                        case "switch":
                            vh.platformIcons[iconIndex].setVisibility(View.VISIBLE);
                            vh.platformIcons[iconIndex].setImageResource(R.drawable.ic_switch);
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

        Log.i(LOG_TAG, "icon:" + item.icon);

        if (item.icon == null || item.icon.length() == 0) {
            vh.image.setImageResource(R.drawable.ic_broken_image);
        } else {
            vh.image.setImageResource(R.drawable.ic_downloading);
            this.picasso
                .load(item.icon)
                .noPlaceholder()
                .error(R.drawable.ic_error)
                .resize(150, 150)
                .centerInside()
                .into(vh.image);
        }

    }
}
