package edu.ranken.brandon_carrillo.game_library.ui.utils;

import android.content.Context;

import androidx.annotation.StringRes;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessageContainer {
    private final Map<String, Integer> messages;

    public ErrorMessageContainer() {
        messages = new HashMap<>();
    }

    public CharSequence getMessages(Context context) {
        StringBuilder sb = new StringBuilder();

        for (Integer messageId : messages.values()) {
            if (messageId != null) {
                sb.append(context.getString(messageId)).append("\n");
            }
        }

        return sb;
    }

    public void setMessage(String key, @StringRes int messageId) {
        messages.put(key, messageId);
    }
}
