package edu.ranken.brandon_carrillo.game_library.ui.utils;

public class SpinnerOption<T> {
    private final String text;
    private final T value;

    public SpinnerOption(String text, T value) {
        this.text = text;
        this.value = value;
    }

    public String toString() {
        return text;
    }

    public String getText() {
        return text;
    }

    public T getValue() {
        return value;
    }
}
