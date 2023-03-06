package com.teskola.molkky;

import android.content.Context;

import java.util.ArrayList;

public class PlayerStats extends PlayerInfo {

    private final DBHandler db;
    private final ArrayList<String> games;
    private int wins = -1;
    private int points = -1;
    private int tossesCount= -1;
    private int eliminations = -1;
    private int excesses = -1;

    public PlayerStats(PlayerInfo player, Context context) {
        super(player.getId(), player.getName());
        db = DBHandler.getInstance(context);
        this.games = db.getGameIds(getId());
    }

    public int getTosses(int value) {
        return db.countTosses(getId(), value);
    }

    public int getGamesCount() {
        return games.size();
    }

    public float getPointsPerToss() {
        if (points == -1) points = db.getTotalPoints(getId());
        if (tossesCount == -1) tossesCount = db.getTotalTosses(getId());
        return this.points / (float) this.tossesCount;
    }

    public float getHitsPct() {
        if (tossesCount == -1) tossesCount = db.getTotalTosses(getId());
        return (tossesCount - getTosses(0)) / (float) tossesCount;
    }

    public void calculateEliminations() {
        int eliminations = 0;
        ArrayList<String> games = db.getGameIds(getId());
        for (String gameId : games) {
            if (db.isEliminated(getId(), gameId)) eliminations++;
        }
        this.eliminations = eliminations;
    }

    public float getEliminationsPct() {
        if (eliminations == -1) calculateEliminations();
        return eliminations / (float) games.size();
    }

    public void calculateExcesses() {
        int excesses = 0;
        for (String gameId : games) {
            Player p = new Player(db.getTosses(gameId, getId()));
            excesses = excesses + p.countExcesses();
        }
        this.excesses = excesses;
    }
    public float getExcessesPerGame () {
        if (excesses == -1) calculateExcesses();
        return excesses / (float) games.size();
    }

    public int getWins() {
        if (wins == -1) wins = db.getWins(getId());
        return wins;
    }
    public int getExcesses() {
        if (excesses == -1) calculateExcesses();
        return excesses;
    }

    public float getWinsPct() {
        if (wins == -1) wins = db.getWins(getId());
        return wins / (float) games.size();
    }

    public int getPoints() {
        if (points == -1) points = db.getTotalPoints(getId());
        return points;
    }
    public int getTossesCount() {
        if (tossesCount == -1) tossesCount = db.getTotalTosses(getId());
        return tossesCount;
    }
    public int getEliminations() {
        if (eliminations == -1) calculateEliminations();
        return eliminations;
    }

}
