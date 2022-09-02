package edu.ranken.brandon_carrillo.game_library.ui.review;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import edu.ranken.brandon_carrillo.game_library.R;

public class ComposeReviewViewModel extends ViewModel {
    // constants
    private static final String LOG_TAG = ComposeReviewViewModel.class.getSimpleName();

    private final FirebaseFirestore db;
    private String gameId;

    // live data
    private final MutableLiveData<String> gameName;
    private final MutableLiveData<Integer> errorMessage;
    private final MutableLiveData<Integer> snackbarMessage;
    private final MutableLiveData<Boolean> finished;

    public ComposeReviewViewModel() {
        db = FirebaseFirestore.getInstance();

        // live data
        gameName = new MutableLiveData<>(null);
        errorMessage = new MutableLiveData<>(null);
        snackbarMessage = new MutableLiveData<>(null);
        finished = new MutableLiveData<>(false);
    }


    public String getGameId() {
        return gameId;
    }

    public LiveData<String> getGameName() { return gameName; }

    public LiveData<Integer> getErrorMessage() { return errorMessage; }

    public LiveData<Integer> getSnackbarMessage() { return snackbarMessage; }

    public LiveData<Boolean> getFinished() { return finished; }

    // remove snackbar message
    public void clearSnackbar() {
        snackbarMessage.postValue(null);
    }

    public void fetchGame(String gameId) {
        this.gameId = gameId;

        db.collection("games")
            .document(gameId)
            .get()
            .addOnCompleteListener((Task<DocumentSnapshot> task) -> {
            if (!task.isSuccessful()) {
                Log.w(LOG_TAG, "Error getting game.", task.getException());
            } else {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    Log.w(LOG_TAG, "Game: " + document.getId() + " does not exist.");
                    errorMessage.postValue(R.string.gameDoesNotExist);
                    snackbarMessage.postValue(R.string.gameDoesNotExist);
                } else {
                    Log.d(LOG_TAG, document.getId() + " => " + document.getData());
                    gameName.postValue(document.getString("name"));
                }
            }
        });
    }

    public void publishReview(String gameId, String reviewText) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // FIXME: show an error if the user is not logged in
        if (user != null) {
            // FIXME: crashes if the user does not have a photo
            String userName = user.getDisplayName();
            String userId = user.getUid();
            String userPhoto = user.getPhotoUrl().toString();
            HashMap<String, Object> review = new HashMap<>();
            review.put("gameId", gameId);
            review.put("userName", userName);
            review.put("userPhoto", userPhoto);
            review.put("userId", userId);
            review.put("reviewText", reviewText);
            review.put("addedOn", FieldValue.serverTimestamp());

            db.collection("reviews")
                .document(userId + ";" + gameId)
                .set(review)
                .addOnCompleteListener((Task<Void> task) -> {
                    if (!task.isSuccessful()) {
                        Log.e(LOG_TAG, "Failed to publish review.", task.getException());
                        snackbarMessage.postValue(R.string.failedToPublishReview);
                    } else {
                        Log.i(LOG_TAG, "Review added.");
                        snackbarMessage.postValue(R.string.reviewAdded);
                        finished.postValue(true);
                    }
                });
        }
    }
}
