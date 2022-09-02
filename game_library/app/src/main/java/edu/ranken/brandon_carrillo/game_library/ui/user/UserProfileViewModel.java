package edu.ranken.brandon_carrillo.game_library.ui.user;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.List;

import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.GameLibrary;
import edu.ranken.brandon_carrillo.game_library.data.GameSummary;
import edu.ranken.brandon_carrillo.game_library.data.GameWishlist;
import edu.ranken.brandon_carrillo.game_library.data.UserProfile;


public class UserProfileViewModel extends ViewModel {
    // constants
    private static final String LOG_TAG = UserProfileViewModel.class.getSimpleName();

    private final FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ListenerRegistration userProfileRegistration;
    private ListenerRegistration libraryRegistration;
    private ListenerRegistration wishlistRegistration;
    private String userId;

    // live data
    private final MutableLiveData<List<GameLibrary>> library;
    private final MutableLiveData<List<GameWishlist>> wishlist;
    private final MutableLiveData<UserProfile> userProfile;
    private final MutableLiveData<Integer> userErrorMessage;
    private final MutableLiveData<Integer> libraryErrorMessage;
    private final MutableLiveData<Integer> wishlistErrorMessage;


    public UserProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // live data
        userProfile = new MutableLiveData<>(null);
        library = new MutableLiveData<>(null);
        wishlist = new MutableLiveData<>(null);
        userErrorMessage = new MutableLiveData<>(null);
        libraryErrorMessage = new MutableLiveData<>(null);
        wishlistErrorMessage = new MutableLiveData<>(null);

    }

    @Override
    protected void onCleared() {
        if (userProfileRegistration != null) {
            userProfileRegistration.remove();
        }
        super.onCleared();
    }

    public String getUserId() {
        return userId;
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<GameLibrary>> getLibrary() { return library; }

    public LiveData<List<GameWishlist>> getWishlist() { return wishlist; }

    public LiveData<Integer> getUserErrorMessage() {
        return userErrorMessage;
    }

    public LiveData<Integer> getLibraryErrorMessage() {
        return libraryErrorMessage;
    }

    public LiveData<Integer> getWishlistErrorMessage() {
        return wishlistErrorMessage;
    }


    public void fetchUserProfile(String userId) {
        this.userId = userId;

        if (userProfileRegistration != null) {
            userProfileRegistration.remove();
        }

        if (userId == null) {
            this.userProfile.postValue(null);
            this.userErrorMessage.postValue(R.string.errorGettingUser);

        } else {
            userProfileRegistration =
                db.collection("users")
                    .document(userId)
                    .addSnapshotListener((document, error) -> {
                        if (error != null) {
                            Log.e(LOG_TAG, "Error getting user.", error);
                            this.userErrorMessage.postValue(R.string.errorGettingUser);

                        } else if (document != null && document.exists()) {
                            UserProfile userProfile = document.toObject(UserProfile.class);
                            this.userProfile.postValue(userProfile);
                            this.userErrorMessage.postValue(null);

                        } else {
                            this.userProfile.postValue(null);
                            this.userErrorMessage.postValue(R.string.userDoesNotExist);

                        }
                    });

            libraryRegistration =
                db.collection("userLibrary")
                    .whereEqualTo("userId", userId)
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
                    .whereEqualTo("userId", userId)
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
        }
    }
}
