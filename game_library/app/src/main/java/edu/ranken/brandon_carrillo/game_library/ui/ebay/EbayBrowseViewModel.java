package edu.ranken.brandon_carrillo.game_library.ui.ebay;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import edu.ranken.brandon_carrillo.game_library.R;
import edu.ranken.brandon_carrillo.game_library.data.Game;
import edu.ranken.brandon_carrillo.game_library.data.GameSummary;
import edu.ranken.brandon_carrillo.game_library.data.ebay.AuthAPI;
import edu.ranken.brandon_carrillo.game_library.data.ebay.AuthEnvironment;
import edu.ranken.brandon_carrillo.game_library.data.ebay.EbayBrowseAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EbayBrowseViewModel extends ViewModel {
    // constants
    private static final String LOG_TAG = EbayBrowseViewModel.class.getSimpleName();


    private static final String clientId = "BrandonC-GameLibr-PRD-794db0712-d4691f63";
    private static final String clientSecret = "PRD-94db071242f6-794e-4124-afa8-8e21";


    private final FirebaseFirestore db;
    private String gameId;
    private AuthAPI authAPI;
    private String authToken;
    private EbayBrowseAPI browseApi;

    // live data
    private final MutableLiveData<Game> game;
    private final MutableLiveData<List<EbayBrowseAPI.ItemSummary>> items;
    private final MutableLiveData<Integer> gameError;
    private final MutableLiveData<Integer> resultsError;
    private final MutableLiveData<Integer> snackbarMessage;

    public EbayBrowseViewModel() {
        db = FirebaseFirestore.getInstance();
        browseApi = new EbayBrowseAPI(AuthEnvironment.PRODUCTION);
        authAPI = new AuthAPI(AuthEnvironment.PRODUCTION, clientId, clientSecret);

        // live data
        game = new MutableLiveData<>(null);
        items = new MutableLiveData<>(null);
        gameError = new MutableLiveData<>(null);
        resultsError = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);
    }


    public String getGameId() {
        return gameId;
    }

    public LiveData<Game> getGame() {
        return game;
    }

    public LiveData<List<EbayBrowseAPI.ItemSummary>> getItems() { return items; }

    public LiveData<Integer> getGameError() {
        return gameError;
    }

    public LiveData<Integer> getResultsError() {
        return resultsError;
    }

    public LiveData<Integer> getSnackbarMessage() {
        return snackbarMessage;
    }

    public void clearSnackbar() {
        snackbarMessage.postValue(null);
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
                                    items.postValue(summaries);
                                    Log.i(LOG_TAG, "items: " + summaries);

                                }
                            } else {
                                Log.e(LOG_TAG, "Search request unsuccessful");
                                resultsError.postValue(R.string.searchRequestUnsuccessful);
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





    public void fetchGame(String gameId) {
        this.gameId = gameId;

        db.collection("games")
            .document(gameId)
            .get()
            .addOnCompleteListener((Task<DocumentSnapshot> task) -> {
                if (!task.isSuccessful()) {
                    Log.w(LOG_TAG, "Error getting game.", task.getException());
                    gameError.postValue(R.string.errorGettingGame);
                    snackbarMessage.postValue(R.string.errorGettingGame);
                } else {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Log.w(LOG_TAG, "Game: " + document.getId() + " does not exist.");
                        gameError.postValue(R.string.gameDoesNotExist);
                    } else {
                        Game doc = document.toObject(Game.class);
                        game.postValue(doc);

                    }


                }
            });


    }

}
