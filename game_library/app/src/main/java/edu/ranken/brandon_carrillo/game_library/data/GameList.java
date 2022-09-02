package edu.ranken.brandon_carrillo.game_library.data;

public enum GameList {
    ALL_GAMES("ALL GAMES"),
    MY_LIBRARY("My Library"),
    MY_WISHLIST("My Wishlist");

    private final String name;

    GameList(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
