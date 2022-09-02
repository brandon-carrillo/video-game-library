package edu.ranken.brandon_carrillo.game_library.ui.game;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.Game;
import edu.ranken.brandon_carrillo.game_library.data.GameReview;
import edu.ranken.brandon_carrillo.game_library.data.ebay.AuthAPI;
import edu.ranken.brandon_carrillo.game_library.data.ebay.AuthEnvironment;
import edu.ranken.brandon_carrillo.game_library.data.ebay.EbayBrowseAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameDetailsViewModel extends ViewModel {
    // constants
    private static final String LOG_TAG = GameDetailsViewModel.class.getSimpleName();
    private static final String clientId = "BrandonC-GameLibr-PRD-794db0712-d4691f63";
    private static final String clientSecret = "PRD-94db071242f6-794e-4124-afa8-8e21";

    // firebase
    private final FirebaseFirestore db;
    private ListenerRegistration gameRegistration;
    private ListenerRegistration reviewRegistration;
    private String gameId;

    private final AuthAPI authAPI;
    private String authToken;
    private final EbayBrowseAPI browseApi;

    // live data
    private final MutableLiveData<Game> game;
    private final MutableLiveData<List<GameReview>> reviews;
    private final MutableLiveData<Integer> gameError;
    private final MutableLiveData<Integer> gameReviewsError;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<Double> avg;

    public GameDetailsViewModel() {
        db = FirebaseFirestore.getInstance();
        browseApi = new EbayBrowseAPI(AuthEnvironment.PRODUCTION);
        authAPI = new AuthAPI(AuthEnvironment.PRODUCTION, clientId, clientSecret);

        // live data
        game = new MutableLiveData<>(null);
        reviews = new MutableLiveData<>(null);
        gameError = new MutableLiveData<>(null);
        gameReviewsError = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);
        avg = new MutableLiveData<>(null);
    }

    @Override
    protected void onCleared() {
        if (gameRegistration != null) {
            gameRegistration.remove();
        }
        if (reviewRegistration != null) {
            reviewRegistration.remove();
        }
        super.onCleared();
    }

    public String getGameId() {
        return gameId;
    }

    public LiveData<Game> getGame() {
        return game;
    }

    public LiveData<List<GameReview>> getReviews() { return reviews; }

    public LiveData<Integer> getGameError() {
        return gameError;
    }

    public LiveData<Integer> getGameReviewsError() {
        return gameReviewsError;
    }

    public LiveData<Integer> getSnackbarMessage() {
        return snackbarMessage;
    }

    public LiveData<Double> getAvg() {
        return avg;
    }


    // remove snackbar message
    public void clearSnackbar() {
        snackbarMessage.postValue(null);
    }


    public void deleteReview() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String doc = userId + ";" + gameId;
            Log.i(LOG_TAG, "doc: " + doc);


            reviewRegistration =
                db.collection("reviews")
                    .document(doc)
                    .addSnapshotListener((DocumentSnapshot snapshot, FirebaseFirestoreException error) -> {
                        if (error != null) {
                            snackbarMessage.postValue(R.string.errorGettingReviews);
                        } else {
                            if (snapshot != null) {
                                GameReview review = snapshot.toObject(GameReview.class);
                                if (review != null) {
                                    db.collection("reviews")
                                        .document(review.id)
                                        .delete()
                                        .addOnCompleteListener((Task<Void> task) -> {
                                            if (!task.isSuccessful()) {
                                                snackbarMessage.postValue(R.string.errorDeletingReview);
                                            } else {
                                                snackbarMessage.postValue(R.string.reviewPostedSuccessfully);
                                            }
                                        });
                                }
                            }
                        }
                    });

        }
    }


    // fetch a particular game from the database
    public void fetchGame(String gameId) {
        this.gameId = gameId;

        if (gameRegistration != null) {
            gameRegistration.remove();
        }
        if (reviewRegistration != null) {
            reviewRegistration.remove();
        }

        if (gameId == null) {
            this.game.postValue(null);
            this.gameError.postValue(R.string.noGameSelected);
//            this.snackbarMessage.postValue(R.string.noGameSelected);
        } else {
            gameRegistration =
                db.collection("games")
                    .document(gameId)
                    .addSnapshotListener((document, error) -> {
                        if (error != null) {
                            Log.e(LOG_TAG, "Error getting game.", error);
                            this.gameError.postValue(R.string.errorGettingGame);
                            this.snackbarMessage.postValue(R.string.errorGettingGame);
                        } else if (document != null && document.exists()) {
                            Game game = document.toObject(Game.class);
                            this.game.postValue(game);
                            this.gameError.postValue(null);

                        } else {
                            this.game.postValue(null);
                            this.gameError.postValue(R.string.gameDoesNotExist);
                            this.snackbarMessage.postValue(R.string.gameDoesNotExist);
                        }
                    });

            reviewRegistration =
                db.collection("reviews")
                    .whereEqualTo("gameId", gameId)
                    .addSnapshotListener((QuerySnapshot querySnapshot, FirebaseFirestoreException error) -> {
                        if (error != null) {
                            Log.e(LOG_TAG, "Error getting reviews.", error);
                            gameReviewsError.postValue(R.string.errorGettingReviews);
                            snackbarMessage.postValue(R.string.errorGettingReviews);
                        } else {
                            Log.i(LOG_TAG, "Reviews updated.");
                            if (querySnapshot != null) {
                                List<GameReview> newReviews = querySnapshot.toObjects(GameReview.class);

                                reviews.postValue(newReviews);
                            }
                        }

                    });
        }
    }


    public void queryGame(String query, String filter, String sort) {
        authAPI.authenticateAsync(
            (authResponse) -> {
                // Authenticated.
                // save auth token for later
                Log.i(LOG_TAG, "Authenticated.");
                this.authToken = authResponse.access_token;

                browseApi.searchAsync(
                    authToken, query, 139973, filter, sort,  100,
                    new Callback<EbayBrowseAPI.SearchResponse>() {
                        @Override
                        public void onResponse(Call<EbayBrowseAPI.SearchResponse> call, Response<EbayBrowseAPI.SearchResponse> response) {
                            Log.i(LOG_TAG, "On response: " + response.isSuccessful());
                            if (response.isSuccessful()) {
                                EbayBrowseAPI.SearchResponse searchResponse = response.body();
                                if (searchResponse != null) {
                                    List<EbayBrowseAPI.ItemSummary> summaries = searchResponse.itemSummaries;
                                    Log.i(LOG_TAG, "query: " + searchResponse.href);

                                    Log.i(LOG_TAG, "items: " + summaries);

                                    double dprice = 0;

                                    for (int i = 0; i < summaries.size(); i++) {
                                        EbayBrowseAPI.ItemPrice price = summaries.get(i).price;
                                        if (summaries.get(i).price.currency.equals("USD")) {
                                            Log.i(LOG_TAG, "price: " + price.value);
                                            dprice += Double.parseDouble(price.value);
                                            Log.i(LOG_TAG, "dprice: " + dprice);
                                        }
                                    }

                                    double average = dprice / summaries.size();
                                    Log.i(LOG_TAG, "avg: " + avg);

                                    avg.postValue(average);

                                }
                            } else {
                                Log.e(LOG_TAG, "Search request unsuccessful");
                            }
                        }

                        @Override
                        public void onFailure(Call<EbayBrowseAPI.SearchResponse> call, Throwable t) {
                            Log.e(LOG_TAG, "On failure: ", t);
                        }
                    });
            },
            (ex) -> {
                // Auth Error.
                // show error message
                Log.e(LOG_TAG, "Auth Error.", ex);
            }
        );


    }



}

