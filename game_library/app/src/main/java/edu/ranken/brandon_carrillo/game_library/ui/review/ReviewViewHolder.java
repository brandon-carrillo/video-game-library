package edu.ranken.brandon_carrillo.game_library.ui.review;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ReviewViewHolder extends RecyclerView.ViewHolder {
    public ImageView userPhoto;
    public TextView userName;
    public TextView addedOn;
    public TextView reviewText;
    public ImageButton deleteButton;
    public ReviewViewHolder(View itemView) { super(itemView); }
}
