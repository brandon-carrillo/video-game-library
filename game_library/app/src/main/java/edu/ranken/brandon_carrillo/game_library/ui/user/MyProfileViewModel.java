package edu.ranken.brandon_carrillo.game_library.ui.user;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.GameLibrary;
import edu.ranken.brandon_carrillo.game_library.data.GameLibraryValue;
import edu.ranken.brandon_carrillo.game_library.data.GameSummary;
import edu.ranken.brandon_carrillo.game_library.data.GameWishlist;
import edu.ranken.brandon_carrillo.game_library.data.GameWishlistValue;
import edu.ranken.brandon_carrillo.game_library.data.UserProfile;

public class MyProfileViewModel extends ViewModel {
    // constants
    private static final String LOG_TAG = MyProfileViewModel.class.getSimpleName();

    // firebase
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private ListenerRegistration userProfileRegistration;
    private ListenerRegistration libraryRegistration;
    private ListenerRegistration wishlistRegistration;

    // live data
    private final MutableLiveData<FirebaseUser> user;
    private final MutableLiveData<UserProfile> userProfile;
    private final MutableLiveData<Uri> downloadUrl;
    private final MutableLiveData<List<GameLibrary>> library;
    private final MutableLiveData<List<GameWishlist>> wishlist;
    private final MutableLiveData<Integer> userErrorMessage;
    private final MutableLiveData<Integer> photoErrorMessage;
    private final MutableLiveData<Integer> libraryErrorMessage;
    private final MutableLiveData<Integer> wishlistErrorMessage;
    private final MutableLiveData<Integer> snackbarMessage;

    private String userId;

    public MyProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // live data
        user = new MutableLiveData<>(currentUser);
        userProfile = new MutableLiveData<>(null);
        library = new MutableLiveData<>(null);
        wishlist = new MutableLiveData<>(null);
        downloadUrl = new MutableLiveData<>();
        userErrorMessage = new MutableLiveData<>(null);
        photoErrorMessage = new MutableLiveData<>(null);
        libraryErrorMessage = new MutableLiveData<>(null);
        wishlistErrorMessage = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);

        if (currentUser != null) {
            userId = currentUser.getUid();

        userProfileRegistration =
            db.collection("users")
                .document(userId)
                .addSnapshotListener((document, error) -> {
                    if (error != null) {
                        userErrorMessage.postValue(R.string.errorGettingUserProfile);
                    } else {
                        if (document != null && document.exists()) {
                            UserProfile myProfile = document.toObject(UserProfile.class);
                            this.userProfile.postValue(myProfile);
                        }
                    }
                });

        libraryRegistration =
            db.collection("userLibrary")
                .whereEqualTo("userId", currentUser.getUid())
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting library.", error);
                        libraryErrorMessage.postValue(R.string.errorGettingLibrary);
                    } else {
                        List<GameLibrary> myLibrary = new ArrayList<>();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                GameLibrary item = document.toObject(GameLibrary.class);
                                myLibrary.add(item);
                            }
                            library.postValue(myLibrary);
                        }
                    }
                });

        wishlistRegistration =
            db.collection("userWishlist")
                .whereEqualTo("userId", currentUser.getUid())
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting wishlist.", error);
                        wishlistErrorMessage.postValue(R.string.errorGettingWishlist);
                    } else {
                        List<GameWishlist> myWishlist = new ArrayList<>();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                GameWishlist item = document.toObject(GameWishlist.class);
                                myWishlist.add(item);
                            }
                            wishlist.postValue(myWishlist);
                        }
                    }
                });

        } else {
            userErrorMessage.postValue(R.string.userNotLoggedIn);
        }

    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public LiveData<FirebaseUser> getUser() { return user; }

    public LiveData<UserProfile> getUserProfile() { return userProfile; }

    public LiveData<List<GameLibrary>> getLibrary() { return library; }

    public LiveData<List<GameWishlist>> getWishlist() { return wishlist; }

    public LiveData<Integer> getUserErrorMessage() {
        return userErrorMessage;
    }

    public LiveData<Integer> getPhotoErrorMessage() {
        return photoErrorMessage;
    }

    public LiveData<Integer> getLibraryErrorMessage() {
        return libraryErrorMessage;
    }

    public LiveData<Integer> getWishlistErrorMessage() {
        return wishlistErrorMessage;
    }

    public LiveData<Integer> getSnackbarMessage() {
        return snackbarMessage;
    }

    public void clearSnackbar() {
        snackbarMessage.postValue(null);
    }


    public void savePlatforms(Map<String, Boolean> platforms) {
            db.collection("users")
                .document(userId)
                .update("platforms", platforms)
                .addOnCompleteListener((Task<Void> task) -> {
                    if (!task.isSuccessful()) {
                        this.snackbarMessage.postValue(R.string.failedToSavePlatforms);
                    } else {
                        Log.i(LOG_TAG, "Saved platforms.");
                    }
                });
    }


    public void uploadProfileImage(Uri profileImageUri) {
        String userId = FirebaseAuth.getInstance().getUid();
        StorageReference storageRef = storage.getReference("/user/" + userId + "/profilePhoto");

        storageRef
            .putFile(profileImageUri)
            .addOnCompleteListener((task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Failed to upload image to: " + storageRef.getPath(), task.getException());
                    photoErrorMessage.postValue(R.string.failedToUploadImage);
                } else {
                    Log.i(LOG_TAG, "Uploaded image to: " + storageRef.getPath());
                    getProfileImageDownloadUrl(storageRef);
                }
            });
    }

    public void getProfileImageDownloadUrl(StorageReference storageRef) {
        storageRef.getDownloadUrl()
            .addOnCompleteListener((downloadTask) -> {
                if (!downloadTask.isSuccessful()) {
                    Log.e(LOG_TAG, "Failed to get download url for: " + storageRef.getPath(), downloadTask.getException());
                    photoErrorMessage.postValue(R.string.failedToGetDownloadUrl);
                } else {
                    Uri downloadUrl = downloadTask.getResult();
                    Log.i(LOG_TAG, "download url: " + downloadUrl);
                    photoErrorMessage.postValue(null);
                    this.downloadUrl.postValue(downloadUrl);
                    updateProfilePhoto(downloadUrl);
                }
            });
    }

    public void updateProfilePhoto(Uri uri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(LOG_TAG, "Cannot update profile photo because user is not authenticated.");
            photoErrorMessage.postValue(R.string.cannotUpdateProfilePhoto);
        } else {
            photoErrorMessage.postValue(null);

            UserProfileChangeRequest request =
                new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build();

            HashMap<String, Object> update = new HashMap<>();
            update.put("photoUrl", uri.toString());

            currentUser
                .updateProfile(request)
                .addOnSuccessListener((result) -> {
                    Log.i(LOG_TAG, "Profile photo updated in auth.");

                    db.collection("users")
                        .document(currentUser.getUid())
                        .set(update, SetOptions.merge())
                        .addOnSuccessListener((result2) -> {
                            Log.i(LOG_TAG, "Profile photo updated in db.");
                        })
                        .addOnFailureListener((error2) -> {
                            Log.e(LOG_TAG, "Failed to update profile photo in db.", error2);
                        });
                })
                .addOnFailureListener((error) -> {
                    Log.e(LOG_TAG, "Failed to update profile photo in auth.", error);
                });
        }
    }
}
