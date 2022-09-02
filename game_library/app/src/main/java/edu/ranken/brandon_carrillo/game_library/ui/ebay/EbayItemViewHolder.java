package edu.ranken.brandon_carrillo.game_library.ui.ebay;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import edu.ranken.brandon_carrillo.game_library.data.ebay.EbayBrowseAPI;

public class EbayItemViewHolder extends RecyclerView.ViewHolder {
    public ImageView itemImage;
    public TextView itemName;
    public TextView itemPrice;
    public TextView itemCondition;
    public TextView itemSeller;
    public TextView itemShippingCost;

    public String itemWebUrl;

    public EbayItemViewHolder(View itemView) { super(itemView); }
}
