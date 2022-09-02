package edu.ranken.brandon_carrillo.game_library.ui.game;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class GameViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView developer;
    public ImageView image;
    public ImageButton library;
    public ImageButton wishlist;
    public Boolean libraryValue;
    public Boolean wishlistValue;

    public ImageView[] platformIcons;

    public GameViewHolder(View itemView) { super(itemView); }
}
