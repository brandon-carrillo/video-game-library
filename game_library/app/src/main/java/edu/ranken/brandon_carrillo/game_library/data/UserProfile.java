package edu.ranken.brandon_carrillo.game_library.data;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Map;

public class UserProfile {
    @DocumentId
    public String userId;
    public String displayName;
    public String photoUrl;
    public Map<String, Boolean> platforms;
    @ServerTimestamp
    public Date lastLogin;

    // empty constructor used for serialization
    public UserProfile() {}
}
