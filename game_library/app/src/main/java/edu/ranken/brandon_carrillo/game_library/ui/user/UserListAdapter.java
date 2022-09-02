package edu.ranken.brandon_carrillo.game_library.ui.user;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import edu.ranken.brandon_carrillo.game_library.GameDetailsActivity;
import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.UserProfileActivity;
import edu.ranken.brandon_carrillo.game_library.data.GameSummary;
import edu.ranken.brandon_carrillo.game_library.data.UserProfile;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameListAdapter;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameListViewModel;

public class UserListAdapter extends RecyclerView.Adapter<UserViewHolder> {
    // constants
    private static final String LOG_TAG = UserListAdapter.class.getSimpleName();

    private final FragmentActivity context;
    private final LayoutInflater layoutInflater;
    private final Picasso picasso;
    private final UserListViewModel model;
    private List<UserProfile> items;

    public UserListAdapter(FragmentActivity context, UserListViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.picasso = Picasso.get();
        this.model = model;
    }

    public void setItems(List<UserProfile> items) {
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
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_user, parent, false);

        UserViewHolder vh = new UserViewHolder(itemView);
        vh.image = itemView.findViewById(R.id.userImage);
        vh.displayName = itemView.findViewById(R.id.userDisplayName);

        vh.itemView.setOnClickListener((view) -> {
            UserProfile user = items.get(vh.getAdapterPosition());
            Log.i(LOG_TAG, "Clicked on user: " + user.userId);

            model.setSelectedUser(user);
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder vh, int position) {
        UserProfile user = items.get(position);

        if (user.photoUrl == null || user.photoUrl.length() == 0) {
            vh.image.setImageResource(R.drawable.ic_broken_image);
        } else {
            vh.image.setImageResource(R.drawable.ic_downloading);
            this.picasso
                .load(user.photoUrl)
                .noPlaceholder()
                .error(R.drawable.ic_error)
                .resize(50, 50)
                .centerInside()
                .into(vh.image);
        }

        if (user.displayName == null || user.displayName.length() == 0) {
            vh.displayName.setText(R.string.displayNameMissing);
        } else {
            vh.displayName.setText(user.displayName);
        }
    }
}
