package edu.ranken.brandon_carrillo.game_library.data;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class GameReview {
    @DocumentId
    public String id;
    public String gameId;
    public String userId;
    public String userName;
    public String userPhoto;
    public String reviewText;

    @ServerTimestamp
    public Date addedOn;
}
