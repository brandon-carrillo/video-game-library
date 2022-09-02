package edu.ranken.brandon_carrillo.game_library.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.R;

public class UserProfileFragment extends Fragment {
    // constants
    private static final String LOG_TAG = UserProfileFragment.class.getSimpleName();


    // views
    private TextView userProfileErrorText;
    private ImageView userProfilePhoto;
    private TextView userIdText;
    private TextView userNameText;
    private ImageView[] platformIcons;
    private TextView userLastLoginText;
    private TextView userLibraryErrorText;
    private RecyclerView library;
    private TextView userWishlistErrorText;
    private RecyclerView wishlist;
    private String userId;

    // state
    private UserProfileViewModel model;
    private UserLibraryAdapter libraryAdapter;
    private UserWishlistAdapter wishlistAdapter;

    public UserProfileFragment() {
        super(R.layout.user_profile_scroll);
    }

    @Override
    public void onViewCreated(@NonNull View contentView, @Nullable Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onViewCreated");
        super.onViewCreated(contentView, savedInstanceState);

        // find views
        userProfileErrorText = contentView.findViewById(R.id.userProfileErrorText);
        userProfilePhoto = contentView.findViewById(R.id.userProfilePhoto);
        userIdText = contentView.findViewById(R.id.userIdText);
        userNameText = contentView.findViewById(R.id.userNameText);
        platformIcons = new ImageView[] {
            contentView.findViewById(R.id.userPlatform1),
            contentView.findViewById(R.id.userPlatform2),
            contentView.findViewById(R.id.userPlatform3),
            contentView.findViewById(R.id.userPlatform4)
        };
        userLastLoginText = contentView.findViewById(R.id.userLastLoginText);
        userLibraryErrorText = contentView.findViewById(R.id.userLibraryErrorText);
        library = contentView.findViewById(R.id.userLibrary);
        userWishlistErrorText = contentView.findViewById(R.id.userWishlistErrorText);
        wishlist = contentView.findViewById(R.id.userWishlist);

        FragmentActivity activity = getActivity();

        // setup recyclerView
        library.setLayoutManager(new LinearLayoutManager(activity));
        wishlist.setLayoutManager(new LinearLayoutManager(activity));


        libraryAdapter = new UserLibraryAdapter(activity, null);
        wishlistAdapter = new UserWishlistAdapter(activity, null);
        library.setAdapter(libraryAdapter);
        wishlist.setAdapter(wishlistAdapter);

        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        model = new ViewModelProvider(activity).get(UserProfileViewModel.class);

        this.userId = model.getUserId();
        model.fetchUserProfile(userId);
        model.getUserProfile().observe(lifecycleOwner, (user) -> {

            if (user != null) {
                Log.i(LOG_TAG, "user: " + user.userId);
                userProfileErrorText.setText("");

                if (user.photoUrl == null) {
                    userProfilePhoto.setImageResource(R.drawable.ic_broken_image);
                } else {
                    userProfilePhoto.setImageResource(R.drawable.ic_downloading);
                    Picasso
                        .get()
                        .load(user.photoUrl)
                        .noPlaceholder()
                        .error(R.drawable.ic_error)
                        .resize(150, 150)
                        .centerInside()
                        .into(userProfilePhoto);
                }

                if (user.userId == null) {
                    userIdText.setText("USER ID MISSING");
                } else {
                    userIdText.setText(user.userId);
                }

                if (user.displayName == null) {
                    userNameText.setText("NAME MISSING");
                } else {
                    userNameText.setText(user.displayName);
                }

                if (user.lastLogin == null) {
                    userLastLoginText.setText("LAST LOGIN MISSING");
                } else {
                    String df = DateFormat.getDateTimeInstance().format(user.lastLogin);
                    userLastLoginText.setText(df);
                }


                if (user.platforms == null) {
                    for (int i = 0; i < platformIcons.length; ++i) {
                        platformIcons[i].setImageResource(0);
                        platformIcons[i].setVisibility(View.GONE);
                    }
                } else {

                    int iconIndex = 0;
                    for (Map.Entry<String, Boolean> entry : user.platforms.entrySet()) {
                        if (Objects.equals(entry.getValue(), Boolean.TRUE)) {
                            switch (entry.getKey()) {
                                default:
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_error);
                                    platformIcons[iconIndex].setContentDescription(getString(R.string.unknownPlatform));
                                    break;
                                case "playstation":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_playstation);
                                    platformIcons[iconIndex].setContentDescription(getString(R.string.playStation));
                                    break;
                                case "xbox":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_xbox);
                                    platformIcons[iconIndex].setContentDescription(getString(R.string.xbox));
                                    break;
                                case "windows":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_windows);
                                    platformIcons[iconIndex].setContentDescription(getString(R.string.windows));
                                    break;
                                case "switch":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_switch);
                                    platformIcons[iconIndex].setContentDescription(getString(R.string.nSwitch));
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


            } else {
                userProfileErrorText.setText(R.string.userMissing);
            }
        });
        model.getUserErrorMessage().observe(lifecycleOwner, (userError) -> {
            userProfileErrorText.setVisibility(userError != null ? View.VISIBLE : View.GONE);
            if (userError != null) {
                userProfileErrorText.setText(userError);
            }
        });
        model.getLibraryErrorMessage().observe(lifecycleOwner, (libraryError) -> {
            userLibraryErrorText.setVisibility(libraryError != null ? View.VISIBLE : View.GONE);
            if (libraryError != null) {
                userLibraryErrorText.setText(libraryError);
            }
        });
        model.getWishlistErrorMessage().observe(lifecycleOwner, (wishlistError) -> {
            userWishlistErrorText.setVisibility(wishlistError != null ? View.VISIBLE : View.GONE);
            if (wishlistError != null) {
                userWishlistErrorText.setText(wishlistError);
            }
        });
        model.getLibrary().observe(lifecycleOwner, (library) -> {
            libraryAdapter.setItems(library);
        });
        model.getWishlist().observe(lifecycleOwner, (wishlist) -> {
            wishlistAdapter.setItems(wishlist);
        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("userId", userId);
    }
}
