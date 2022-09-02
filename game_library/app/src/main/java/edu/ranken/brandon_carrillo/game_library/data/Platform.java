package edu.ranken.brandon_carrillo.game_library.data;

import com.google.firebase.firestore.DocumentId;

public class Platform {
    @DocumentId
    public String id;
    public String name;
    public String icon;

    @Override
    public String toString() {
        return "Platform {" + id + ", " + name + "}";
    }
}
