package edu.ranken.brandon_carrillo.game_library.ui.ebay;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.ebay.EbayBrowseAPI;

public class EbayItemListAdapter extends RecyclerView.Adapter<EbayItemViewHolder> {
    // constants
    private static final String LOG_TAG = EbayItemListAdapter.class.getSimpleName();
    private List<EbayBrowseAPI.ItemSummary> items;



    private AppCompatActivity context;
    private final LayoutInflater layoutInflater;
    private final Picasso picasso;
    private EbayBrowseViewModel model;

    public EbayItemListAdapter(AppCompatActivity context, EbayBrowseViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.picasso = Picasso.get();
        this.model = model;
    }

    public void setItems(List<EbayBrowseAPI.ItemSummary> items) {
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
    public EbayItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_ebay_listing, parent, false);

        EbayItemViewHolder vh = new EbayItemViewHolder(itemView);
        vh.itemImage = itemView.findViewById(R.id.ebayItemImage);
        vh.itemName = itemView.findViewById(R.id.ebayItemName);
        vh.itemPrice = itemView.findViewById(R.id.ebayItemPrice);
        vh.itemCondition = itemView.findViewById(R.id.ebayItemCondition);
        vh.itemSeller = itemView.findViewById(R.id.ebayItemSeller);
        vh.itemShippingCost = itemView.findViewById(R.id.ebayItemShippingCost);


        vh.itemView.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "Ebay Listing clicked. " + vh.itemWebUrl);
            if (vh.itemWebUrl == null || vh.itemWebUrl.length() == 0) {
                Snackbar.make(vh.itemView, R.string.noUrlForListing, Snackbar.LENGTH_SHORT).show();
            } else {
                Uri itemWebUri = Uri.parse(vh.itemWebUrl);
                context.startActivity(new Intent(Intent.ACTION_VIEW, itemWebUri));
            }

        });

        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull EbayItemViewHolder vh, int position) {
        EbayBrowseAPI.ItemSummary itemSummary = items.get(position);


        if (itemSummary.image == null) {
            vh.itemImage.setImageResource(R.drawable.ic_broken_image);
        } else {
            if (itemSummary.image.imageUrl == null || itemSummary.image.imageUrl.length() == 0) {
                vh.itemImage.setImageResource(R.drawable.ic_broken_image);
            } else {
                vh.itemImage.setImageResource(R.drawable.ic_downloading);
                this.picasso
                    .load(itemSummary.image.imageUrl)
                    .noPlaceholder()
                    .error(R.drawable.ic_error)
                    .resize(200, 200)
                    .centerInside()
                    .into(vh.itemImage);
            }
        }

        if (itemSummary.title == null || itemSummary.title.length() == 0) {
            vh.itemName.setText(R.string.itemNameMissing);
        } else {
            vh.itemName.setText(itemSummary.title);
        }

        if (itemSummary.price.value == null || itemSummary.price.value.length() == 0) {
            vh.itemPrice.setText(R.string.itemPriceMissing);
        } else {
            String price = "$" + itemSummary.price.value;
            vh.itemPrice.setText(price);
        }

        if (itemSummary.seller.username == null || itemSummary.seller.username.length() == 0) {
            vh.itemSeller.setText(R.string.itemSellerMissing);
        } else {
            String seller = itemSummary.seller.username + " (" + itemSummary.seller.feedbackPercentage + "%)";
            vh.itemSeller.setText(seller);
        }



        String condition = "(" + itemSummary.condition + ")";
        vh.itemCondition.setText(condition);

        if (itemSummary.shippingOptions != null) {


            if (itemSummary.shippingOptions.get(0).shippingCostType.equals("FIXED")) {

                if (itemSummary.shippingOptions.get(0).shippingCost.value.equals("0.00")) {
                    vh.itemShippingCost.setText(R.string.freeShipping);
                } else {
                    String shipping = "+ $" + itemSummary.shippingOptions.get(0).shippingCost.value + " shipping";
                    vh.itemShippingCost.setText(shipping);
                }
            } else {
                vh.itemShippingCost.setText(R.string.calculatedAtCheckout);
            }
        } else {
            vh.itemShippingCost.setText(R.string.calculatedAtCheckout);
        }


        vh.itemWebUrl = itemSummary.itemWebUrl;

    }
}
