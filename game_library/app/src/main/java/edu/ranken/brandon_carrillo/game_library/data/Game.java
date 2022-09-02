package edu.ranken.brandon_carrillo.game_library.data;

import com.google.firebase.firestore.DocumentId;

import java.util.Map;

public class Game {
    @DocumentId
    public String id;
    public String name;
    public String developer;
    public Integer releaseYear;
    public String description;
    public String genre;
    public String controllerSupport;
    public String multiplayerSupport;
    public String tags;
    public Map<String, String> ebay;
    public String image;
    public String icon;
    public String banner;
    public String screenshot1;
    public String screenshot2;
    public String screenshot3;
    public Map<String, Boolean> supportedPlatforms;
}
