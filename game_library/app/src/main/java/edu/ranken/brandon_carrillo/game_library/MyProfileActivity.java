package edu.ranken.brandon_carrillo.game_library;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.ranken.brandon_carrillo.game_library.data.UserProfile;
import edu.ranken.brandon_carrillo.game_library.ui.PlatformChooserDialog;
import edu.ranken.brandon_carrillo.game_library.ui.user.MyLibraryAdapter;
import edu.ranken.brandon_carrillo.game_library.ui.user.MyProfileViewModel;
import edu.ranken.brandon_carrillo.game_library.ui.user.MyWishlistAdapter;

public class MyProfileActivity extends BaseActivity {
    // constants
    private static final String LOG_TAG = MyProfileActivity.class.getSimpleName();
    public static final String EXTRA_USER_ID = "userId";

    // views
    private TextView myProfileErrorText;
    private ImageView myProfilePhoto;
    private TextView myIdText;
    private TextView myNameText;
    private TextView myEmailText;
    private ImageButton cameraButton;
    private ImageButton galleryButton;
    private TextView myStatusText;
    private Button setPlatformsButton;
    private ImageView[] platformIcons;
    private TextView myProfilePhotoErrorText;
    private TextView myLibraryErrorText;
    private RecyclerView myLibrary;
    private TextView myWishlistErrorText;
    private RecyclerView myWishlist;
    private FloatingActionButton shareMyProfileButton;

    // state
    private MyProfileViewModel model;
    private MyLibraryAdapter myLibraryAdapter;
    private MyWishlistAdapter myWishlistAdapter;
    private File outputImageFile;
    private Uri outputImageUri;
    private FirebaseUser myProfile;
    private UserProfile profile;

    // launcher
    private final ActivityResultLauncher<String> getContentLauncher =
        registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            (Uri uri) -> {
                if (uri != null) {
                    uploadImage(uri);
                }
            }
        );

    private final ActivityResultLauncher<Uri> takePictureLauncher =
        registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            (Boolean result) -> {
                // Handle captured image
                Log.i(LOG_TAG, "take picture result: " + result);
                if (Objects.equals(result, Boolean.TRUE)) {
                    // upload image
                    uploadImage(outputImageUri);
                } else {
                    Log.e(LOG_TAG, "failed to return picture");
                }
            }
        );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile_scroll);

        // find views
        myProfileErrorText = findViewById(R.id.myProfileErrorText);
        myProfilePhoto = findViewById(R.id.myProfilePhoto);
        myIdText = findViewById(R.id.myIdText);
        myNameText = findViewById(R.id.myNameText);
        myEmailText = findViewById(R.id.myEmailText);
        cameraButton = findViewById(R.id.myProfileCameraButton);
        galleryButton = findViewById(R.id.myProfileGalleryButton);
        myStatusText = findViewById(R.id.myStatusText);
        myProfilePhotoErrorText = findViewById(R.id.myProfilePhotoErrorText);
        setPlatformsButton = findViewById(R.id.setPlatformsButton);
        platformIcons = new ImageView[] {
            findViewById(R.id.myPlatform1),
            findViewById(R.id.myPlatform2),
            findViewById(R.id.myPlatform3),
            findViewById(R.id.myPlatform4)
        };
        myLibraryErrorText = findViewById(R.id.myLibraryErrorText);
        myLibrary = findViewById(R.id.myLibrary);
        myWishlistErrorText = findViewById(R.id.myWishlistErrorText);
        myWishlist = findViewById(R.id.myWishlist);
        shareMyProfileButton = findViewById(R.id.shareMyProfileButton);


        // disable the camera if not available
        boolean hasCamera =
            this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        cameraButton.setVisibility(hasCamera ? View.VISIBLE : View.GONE);

        cameraButton.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "camera");
            try {
                outputImageFile = createImageFile();
                Log.i(LOG_TAG, "outputImageFile = " + outputImageFile);
                outputImageUri = fileToUri(outputImageFile);
                Log.i(LOG_TAG, "outputImageUri = " + outputImageUri);
                takePictureLauncher.launch(outputImageUri);
            } catch (Exception ex) {
                Log.e(LOG_TAG, "take picture failed", ex);
            }
        });
        galleryButton.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "gallery");

            getContentLauncher.launch("image/*");
        });


        // setup recyclerView
        myLibrary.setLayoutManager(new LinearLayoutManager(this));
        myWishlist.setLayoutManager(new LinearLayoutManager(this));

        // setup view model & adapter
        model = new ViewModelProvider(this).get(MyProfileViewModel.class);
        myLibraryAdapter = new MyLibraryAdapter(this, null);
        myWishlistAdapter = new MyWishlistAdapter(this, null);
        myLibrary.setAdapter(myLibraryAdapter);
        myWishlist.setAdapter(myWishlistAdapter);

        model.getUser().observe(this, (user) -> {
            myProfile = user;

            if (user != null) {

                String verified;
                if (user.isEmailVerified()) {
                    verified = "Verified";
                } else {
                    verified = "Unverified";
                }
                myStatusText.setText(verified);

                // set views
                myIdText.setText(user.getUid());
                myNameText.setText(user.getDisplayName());
                myEmailText.setText(user.getEmail());

                Uri photoUrl = user.getPhotoUrl();
                if (photoUrl != null) {
                    myProfilePhoto.setImageResource(R.drawable.ic_downloading);
                    Picasso
                        .get()
                        .load(photoUrl)
                        .noPlaceholder()
                        .error(R.drawable.ic_error)
                        .resize(300, 300)
                        .centerInside()
                        .into(myProfilePhoto);
                } else {
                    myProfilePhoto.setImageResource(R.drawable.ic_broken_image);
                }
            } else {
                myProfileErrorText.setText("Error getting user");
            }
        });

        model.getUserProfile().observe(this, (userProfile) -> {
            profile = userProfile;

            if (userProfile == null) {

            } else {
                if (userProfile.platforms == null) {
                    for (int i = 0; i < platformIcons.length; ++i) {
                        platformIcons[i].setImageResource(0);
                        platformIcons[i].setVisibility(View.GONE);
                    }
                } else {

//                    HashMap<String, Boolean> map = new HashMap<>(userProfile.platforms);


                    int iconIndex = 0;
                    for (Map.Entry<String, Boolean> entry : userProfile.platforms.entrySet()) {
                        if (Objects.equals(entry.getValue(), Boolean.TRUE)) {
                            switch (entry.getKey()) {
                                default:
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_error);
                                    break;
                                case "playstation":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_playstation);
                                    break;
                                case "xbox":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_xbox);
                                    break;
                                case "windows":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_windows);
                                    break;
                                case "switch":
                                    platformIcons[iconIndex].setVisibility(View.VISIBLE);
                                    platformIcons[iconIndex].setImageResource(R.drawable.ic_switch);
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
            }
        });

        model.getLibrary().observe(this, (library) -> {
            myLibraryAdapter.setItems(library);
        });
        model.getWishlist().observe(this, (wishlist) -> {
            myWishlistAdapter.setItems(wishlist);
        });
        model.getUserErrorMessage().observe(this, (userError) -> {
            myProfileErrorText.setVisibility(userError != null ? View.VISIBLE : View.GONE);
            if (userError != null) {
                myProfileErrorText.setText(userError);
            }
        });
        model.getPhotoErrorMessage().observe(this, (photoError) -> {
            myProfilePhotoErrorText.setVisibility(photoError != null ? View.VISIBLE : View.GONE);
            if (photoError != null) {
                myProfilePhotoErrorText.setText(photoError);
            }
        });
        model.getLibraryErrorMessage().observe(this, (libraryError) -> {
            myLibraryErrorText.setVisibility(libraryError != null ? View.VISIBLE : View.GONE);
            if (libraryError != null) {
                myLibraryErrorText.setText(libraryError);
            }
        });
        model.getWishlistErrorMessage().observe(this, (wishlistError) -> {
            myWishlistErrorText.setVisibility(wishlistError != null ? View.VISIBLE : View.GONE);
            if (wishlistError != null) {
                myWishlistErrorText.setText(wishlistError);
            }
        });
        model.getSnackbarMessage().observe(this, (snackbarMessage) -> {
            if (snackbarMessage != null) {
                Snackbar.make(myIdText, snackbarMessage, Snackbar.LENGTH_SHORT).show();
                model.clearSnackbar();
            }
        });

        // register listeners
        setPlatformsButton.setOnClickListener((view) -> {
            Context context = this;

            PlatformChooserDialog dialog = new PlatformChooserDialog(
                context,
                R.string.setPlatforms,
                null,
                profile.platforms,
                selectedPlatforms -> {
                    model.savePlatforms(selectedPlatforms);
                }
            );
            dialog.show();
        });

        shareMyProfileButton.setOnClickListener((view) -> {
            Log.i(LOG_TAG, "Share my profile clicked.");
            String myDisplayName;

            if (myProfile == null) {
                Snackbar.make(view, "No User found.", Snackbar.LENGTH_SHORT).show();
            } else if (myProfile.getDisplayName() == null) {
                Snackbar.make(view, "User does not have a name.", Snackbar.LENGTH_SHORT).show();
            } else {
                if (myProfile.getDisplayName() == null || myProfile.getDisplayName().length() == 0) {
                    myDisplayName = "No Display Name";
                } else {
                    myDisplayName = myProfile.getDisplayName();
                }

                Log.i(LOG_TAG, "id: " + myProfile.getUid());
                String message =
                    "Check out my profile\n" +
                        myDisplayName +
                        "\nhttps://my-game-list.com/user/" + myProfile.getUid();

                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                sendIntent.putExtra(Intent.EXTRA_TITLE, myDisplayName);
                sendIntent.setType("text/plain");

                startActivity(Intent.createChooser(sendIntent, myDisplayName));
            }
        });


    }


    public void uploadImage(Uri uri) {
        Log.i(LOG_TAG, "user picked: " + uri);

        Picasso
            .get()
            .load(uri)
            .resize(300, 300)
            .centerInside()
            .into(myProfilePhoto);

        model.uploadProfileImage(uri);
    }

    private File createImageFile() throws IOException {
        // create file name
        Calendar now = Calendar.getInstance();
        String fileName =
            String.format(Locale.US, "image_%1$tY%1$tm%1$td_%1$tH%1$tM.jpg", now);

        // create paths
        File storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile =
            new File(storageDir, fileName);

        // create the pictures directory, if it does not exist yet
        storageDir.mkdirs();

        // create the file, if it does not exist yet
        imageFile.createNewFile();

        // return File object
        return imageFile;
    }

    private static final String FILE_PROVIDER_AUTHORITY = "edu.ranken.brandon_carrillo.game_library.fileprovider";

    private Uri fileToUri(File file) {
        return FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // force up navigation to have the same behavior as back navigation
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}