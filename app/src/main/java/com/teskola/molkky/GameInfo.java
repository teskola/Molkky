package com.teskola.molkky;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GameInfo implements Comparable<GameInfo> {
    private final long timestamp;
    private final String winner;
    private final String id;

    public GameInfo(String id, long timestamp, String winner) {
        this.id = id;
        this.winner = winner;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public String toString() {
        String timestampString = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(timestamp);
        return timestampString + " (" + this.winner + ")";

    }

    @Override
    public int compareTo(GameInfo game) {
        return Long.compare(game.timestamp, this.timestamp);
    }
}

