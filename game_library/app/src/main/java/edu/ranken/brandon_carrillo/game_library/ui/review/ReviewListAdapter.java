package edu.ranken.brandon_carrillo.game_library.ui.review;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.List;

import edu.ranken.brandon_carrillo.game_library.GameDetailsActivity;
import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.GameReview;
import edu.ranken.brandon_carrillo.game_library.ui.game.GameDetailsViewModel;


public class ReviewListAdapter extends RecyclerView.Adapter<ReviewViewHolder> {
    // constants
    private static final String LOG_TAG = ReviewListAdapter.class.getSimpleName();


    private final Activity context;
    private final LayoutInflater layoutInflater;
    private final GameDetailsViewModel model;
    private List<GameReview> items;

    public ReviewListAdapter(Activity context, GameDetailsViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.model = model;
    }

    public void setItems(List<GameReview> items) {
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
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.review_item, parent, false);
        ReviewViewHolder vh = new ReviewViewHolder(itemView);
        vh.userPhoto = itemView.findViewById(R.id.item_review_photo);
        vh.userName = itemView.findViewById(R.id.item_review_userName);
        vh.addedOn = itemView.findViewById(R.id.item_review_addedOn);
        vh.reviewText = itemView.findViewById(R.id.item_review_text);
        vh.deleteButton = itemView.findViewById(R.id.deleteReviewButton);


        vh.deleteButton.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "delete review clicked." + model);
//            model.deleteReview();
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(R.string.deleteReview);
                builder.setMessage(R.string.areYouSureDelete);
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    model.deleteReview();
                });
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> { /* ... */ });
                AlertDialog dialog = builder.create();
                dialog.show();

        });


        return vh;
    }



    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder vh, int position) {
        GameReview item = items.get(position);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (item.userId != null) {
            if (item.userId.equals(currentUser.getUid())) {
                vh.deleteButton.setVisibility(View.VISIBLE);
            } else {
                vh.deleteButton.setVisibility(View.GONE);
            }
        } else {
            vh.deleteButton.setVisibility(View.GONE);
        }


            if (item.userName == null || item.userName.length() == 0) {
                vh.userName.setText(R.string.noUsername);
            } else {
                vh.userName.setText(item.userName);
            }
            if (item.userPhoto == null || item.userPhoto.length() == 0) {
                vh.userPhoto.setImageResource(R.drawable.ic_broken_image);
            } else {
                Picasso
                    .get()
                    .load(item.userPhoto)
                    .noPlaceholder()
                    .error(R.drawable.ic_error)
                    .resize(50, 50)
                    .centerInside().into(vh.userPhoto);
            }
            if (item.addedOn == null) {
                vh.addedOn.setText(R.string.noDate);
            } else {
                String df = DateFormat.getDateTimeInstance().format(item.addedOn);
                vh.addedOn.setText(df);
            }
            if (item.reviewText == null || item.reviewText.length() == 0) {
                vh.reviewText.setText(R.string.noText);
            } else {
                vh.reviewText.setText(item.reviewText);
            }

    }




}
