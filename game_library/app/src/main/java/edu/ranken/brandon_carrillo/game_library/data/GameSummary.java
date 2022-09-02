package edu.ranken.brandon_carrillo.game_library.data;

import com.google.firebase.firestore.DocumentId;

import java.util.Map;

public class GameSummary {
    public String id;
    public String name;
    public String developer;
    public String image;
    public String icon;
    public Integer releaseYear;
    public Map<String, Boolean> supportedPlatforms;


    public GameSummary() {}

    public GameSummary(Game game) {
        this.id = game.id;
        this.name = game.name;
        this.developer = game.developer;
        this.releaseYear = game.releaseYear;
        this.image = game.image;
        this.icon = game.icon;
        this.supportedPlatforms = game.supportedPlatforms;
    }
}
