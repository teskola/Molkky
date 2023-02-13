package com.example.molkky;

import android.content.Context;

import java.util.ArrayList;

public class PlayerStats extends PlayerInfo {

    private DBHandler db;
    private ArrayList<Integer> games;
    private int wins;
    private int points;
    private int tossesCount;
    private int eliminations;
    private int excesses;

    public PlayerStats(PlayerInfo player, Context context) {
        super(player.getId(), player.getName(), player.getImage());
        db = DBHandler.getInstance(context);
        this.games = db.getGameIds(getId());
        this.wins = db.getWins(getId());
        this.points = db.getTotalPoints(getId());
        this.tossesCount = db.getTotalTosses(getId());
        this.eliminations = calculateEliminations();
        this.excesses = calculateExcesses();
    }

    public int getTosses(int value) {
        return db.countTosses(getId(), value);
    }

    public int getGamesCount() {
        return games.size();
    }

    public float getPointsPerToss() {
        return this.points / (float) this.tossesCount;
    }

    public float getHitsPct() {
        return (tossesCount - getTosses(0)) / (float) tossesCount;
    }

    public int calculateEliminations() {
        int eliminations = 0;
        ArrayList<Integer> games = db.getGameIds(getId());
        for (int gameId : games) {
            if (db.isEliminated(getId(), gameId)) eliminations++;
        }
        return eliminations;
    }

    public float getEliminationsPct() {
        return eliminations / (float) games.size();
    }

    public int calculateExcesses() {
        int excesses = 0;
        for (int gameId : games) {
            Player p = new Player(db.getTosses(gameId, getId()));
            excesses = excesses + p.countExcesses();
        }
        return excesses;
    }
    public float getExcessesPerGame () {
        return excesses / (float) games.size();
    }

    public int getWins() {
        return wins;
    }
    public int getExcesses() {
        return excesses;
    }

    public float getWinsPct() {
        return wins / (float) games.size();
    }

    public int getPoints() {
        return points;
    }
    public int getTossesCount() {
        return tossesCount;
    }
    public int getEliminations() {
        return eliminations;
    }

}
