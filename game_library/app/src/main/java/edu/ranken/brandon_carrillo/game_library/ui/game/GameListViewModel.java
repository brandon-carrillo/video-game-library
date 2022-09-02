package edu.ranken.brandon_carrillo.game_library.ui.game;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.Game;
import edu.ranken.brandon_carrillo.game_library.data.GameLibrary;
import edu.ranken.brandon_carrillo.game_library.data.GameLibraryValue;
import edu.ranken.brandon_carrillo.game_library.data.GameList;
import edu.ranken.brandon_carrillo.game_library.data.GameSummary;
import edu.ranken.brandon_carrillo.game_library.data.GameWishlist;
import edu.ranken.brandon_carrillo.game_library.data.GameWishlistValue;
import edu.ranken.brandon_carrillo.game_library.data.Platform;

public class GameListViewModel extends ViewModel {
    // constants
    private static final String LOG_TAG = GameListViewModel.class.getSimpleName();

    private final FirebaseFirestore db;
    private ListenerRegistration gamesRegistration;
    private ListenerRegistration libraryRegistration;
    private ListenerRegistration wishlistRegistration;
    private ListenerRegistration platformsRegistration;
    private final String userId;
    private String filterPlatformId = null;
    private GameList filterList = GameList.ALL_GAMES;

    // livedata
    private final MutableLiveData<List<GameSummary>> games;
    private final MutableLiveData<List<GameLibraryValue>> libraryValue;
    private final MutableLiveData<List<GameWishlistValue>> wishlistValue;
    private final MutableLiveData<List<Platform>> platforms;
    private final MutableLiveData<Integer> errorMessage;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<GameSummary> selectedGame;

    public GameListViewModel() {
        db = FirebaseFirestore.getInstance();

        // livedata
        games = new MutableLiveData<>(null);
        libraryValue = new MutableLiveData<>(null);
        wishlistValue = new MutableLiveData<>(null);
        platforms = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);
        selectedGame = new MutableLiveData<>(null);

        // get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            userId = null;
        }

        // observe games registration
        queryGames();

        // observe library registration
        libraryRegistration =
            db.collection("userLibrary")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting library.", error);
                        snackbarMessage.postValue(R.string.errorGettingLibrary);
                    } else {
                        Log.i(LOG_TAG, "Library updated.");

                        List<GameLibraryValue> newLibrary = new ArrayList<>();
                        if (querySnapshot != null) {

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String gameId = document.getString("gameId");
                                Boolean value = document.getBoolean("value");
                                if (gameId != null && value != null) {
                                    newLibrary.add(new GameLibraryValue(gameId, value));
                                }
                            }
                            libraryValue.postValue(newLibrary);
                        }
                    }
                });

        // observe wishlist registration
        wishlistRegistration =
            db.collection("userWishlist")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting wishlist.", error);
                        snackbarMessage.postValue(R.string.errorGettingWishlist);
                    } else {
                        Log.i(LOG_TAG, "Wishlist updated.");

                        List<GameWishlistValue> newWishlist = new ArrayList<>();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String gameId = document.getString("gameId");
                                Boolean value = document.getBoolean("value");
                                if (gameId != null && value != null) {
                                    newWishlist.add(new GameWishlistValue(gameId, value));
                                }
                            }
                            wishlistValue.postValue(newWishlist);
                        }
                    }
                });

        // observe platforms collection
        platformsRegistration =
            db.collection("platforms")
                .orderBy("name")
                .addSnapshotListener((@NonNull QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "Error getting platforms.", error);
                        snackbarMessage.postValue(R.string.errorGettingPlatforms);
                    } else {
                        Log.i(LOG_TAG, "Platforms updated.");
                        List<Platform> newPlatforms = querySnapshot.toObjects(Platform.class);
                        platforms.postValue(newPlatforms);
                    }
                });
    }

    @Override
    protected void onCleared() {
        if (gamesRegistration != null) {
            gamesRegistration.remove();
        }
        if (libraryRegistration != null) {
            libraryRegistration.remove();
        }
        if (wishlistRegistration != null) {
            wishlistRegistration.remove();
        }
        if (platformsRegistration != null) {
            platformsRegistration.remove();
        }
        super.onCleared();
    }

    public LiveData<List<GameSummary>> getGames() {
        return games;
    }

    public LiveData<List<GameLibraryValue>> getLibraryValue() {
        return libraryValue;
    }

    public LiveData<List<GameWishlistValue>> getWishlistValue() { return wishlistValue; }

    public LiveData<List<Platform>> getPlatforms() {
        return platforms;
    }

    public LiveData<Integer> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getSnackbarMessage() { return snackbarMessage; }

    public String getFilterPlatformId() {
        return filterPlatformId;
    }

    public void clearSnackbar() {
        snackbarMessage.postValue(null);
    }

    public LiveData<GameSummary> getSelectedGame() { return selectedGame; }

    public void setSelectedGame(GameSummary game) {
        this.selectedGame.postValue(game);
    }


    public void addToLibrary(GameSummary game, Map<String, Boolean> platforms) {
        HashMap<String, Object> library = new HashMap<>();
        library.put("gameId", game.id);
        library.put("userId", userId);
        library.put("addedOn", FieldValue.serverTimestamp());
        library.put("value", true);
        library.put("game", game);
        library.put("selectedPlatforms", platforms);

        db.collection("userLibrary")
            .document(userId + ";" + game.id)
            .set(library)
            .addOnCompleteListener((Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Failed to add to library", task.getException());
                    snackbarMessage.postValue(R.string.failedToAddToLibrary);
                } else {
                    Log.i(LOG_TAG, "Added to library.");
                    snackbarMessage.postValue(R.string.addedToLibrary);
                }
            });
    }

    public void removeFromLibrary(String gameId, boolean showSnackbar) {
        db.collection("userLibrary")
            .document(userId + ";" + gameId)
            .delete()
            .addOnCompleteListener((Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Failed to remove from library.", task.getException());
                    snackbarMessage.postValue(R.string.failedToRemoveFromLibrary);
                } else {
                    Log.i(LOG_TAG, "Removed from library.");
                    if (showSnackbar) {
                        snackbarMessage.postValue(R.string.removedFromLibrary);
                    }
                }
            });
    }

    public void addToWishlist(GameSummary game, Map<String, Boolean> platforms) {
        HashMap<String, Object> wishlist = new HashMap<>();
        wishlist.put("gameId", game.id);
        wishlist.put("userId", userId);
        wishlist.put("addedOn", FieldValue.serverTimestamp());
        wishlist.put("value", true);
        wishlist.put("game", game);
        wishlist.put("selectedPlatforms", platforms);

        db.collection("userWishlist")
            .document(userId + ";" + game.id)
            .set(wishlist)
            .addOnCompleteListener((Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Failed to add to wishlist.", task.getException());
                    snackbarMessage.postValue(R.string.failedToAddToWishlist);
                } else {
                    Log.i(LOG_TAG, "Added to wishlist.");
                    snackbarMessage.postValue(R.string.addedToWishlist);
                }
            });
    }

    public void removeFromWishlist(String gameId, boolean showSnackbar) {
        db.collection("userWishlist")
            .document(userId + ";" + gameId)
            .delete()
            .addOnCompleteListener((Task<Void> task) -> {
                if (!task.isSuccessful()) {
                    Log.e(LOG_TAG, "Failed to remove from wishlist.", task.getException());
                    snackbarMessage.postValue(R.string.failedToRemoveFromWishlist);
                } else {
                    Log.i(LOG_TAG, "Removed from wishlist.");
                    if (showSnackbar) {
                        snackbarMessage.postValue(R.string.removedFromWishlist);
                    }
                }
            });
    }

    public void filterGamesByPlatform(String platformId) {
        this.filterPlatformId = platformId;
        queryGames();
    }

    public void filterGamesByList(GameList list) {
        this.filterList = list;
        queryGames();
    }

    public void queryGames() {
        if (gamesRegistration != null) {
            gamesRegistration.remove();
        }

        Query query;
        switch (filterList) {
            default:
                throw new IllegalStateException("Unsupported Option");
            case ALL_GAMES:
                query = db.collection("games");
                break;
            case MY_LIBRARY:
                query = db.collection("userLibrary")
                            .whereEqualTo("userId", userId);
                break;
            case MY_WISHLIST:
                query = db.collection("userWishlist")
                            .whereEqualTo("userId", userId);
                break;
        }

        if (filterPlatformId != null) {
            if (filterList == GameList.ALL_GAMES) {
                query = query.whereEqualTo("supportedPlatforms." + filterPlatformId, true);
            }
            else {
                query = query.whereEqualTo("game.supportedPlatforms." + filterPlatformId, true);
            }
        }

        gamesRegistration =
            query.addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                if (error != null) {
                    Log.e(LOG_TAG, "Error getting games.", error);
                    errorMessage.postValue(R.string.errorGettingGame);
                    snackbarMessage.postValue(R.string.errorGettingGame);
                } else {
                    ArrayList<GameSummary> newGameSummaries = new ArrayList<>();
                    if (querySnapshot != null) {
                        switch (filterList) {
                            default:
                                throw new IllegalStateException("Unsupported Option");
                            case ALL_GAMES:
                                List<Game> newGames = querySnapshot.toObjects(Game.class);
                                for (Game game : newGames) {
                                    newGameSummaries.add(new GameSummary(game));
                                }
                                break;
                            case MY_LIBRARY:
                                List<GameLibrary> newLibrary = querySnapshot.toObjects(GameLibrary.class);
                                for (GameLibrary library : newLibrary) {
                                    if (library.game != null) {
                                        newGameSummaries.add(library.game);
                                    }
                                }
                                break;
                            case MY_WISHLIST:
                                List<GameWishlist> newWishlist = querySnapshot.toObjects(GameWishlist.class);
                                for (GameWishlist wishlist : newWishlist) {
                                    if (wishlist.game != null) {
                                        newGameSummaries.add(wishlist.game);
                                    }
                                }
                                break;
                        }
                        games.postValue(newGameSummaries);

                        errorMessage.postValue(null);
                        Log.i(LOG_TAG, "Games list updated.");
                    }
                }
            });
    }
}
