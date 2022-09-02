package edu.ranken.brandon_carrillo.game_library.data;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Map;

public class GameWishlist {
    @DocumentId
    public String id;
    public String gameId;
    public String userId;
    public Boolean value;
    public GameSummary game;
    public Map<String, Boolean> selectedPlatforms;

    @ServerTimestamp
    public Date addedOn;
}
