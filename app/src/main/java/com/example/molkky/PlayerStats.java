package com.example.molkky;

import android.content.Context;

import java.util.ArrayList;

public class PlayerStats {
    private final DBHandler db;

    public PlayerStats(Context context) {
        db = DBHandler.getInstance(context);
    }
    public int getGames(int playerId) {
        return db.getGamesCount(playerId);
    }
    public int getWins(int playerId) {
        return db.getWins(playerId);
    }
    public int getWinsPct(int playerId) { return Math.round(100 * getWins(playerId) / (float) getGames(playerId));}
    public int getPoints(int playerId) {
        return db.getTotalPoints(playerId);
    }
    public int getTosses(int playerId) {
        return db.getTotalTosses(playerId);
    }
    public float getPointsPerToss(int playerId) {
        return db.getTotalPoints(playerId) / (float) db.getTotalTosses(playerId);
    }

    public int getTosses (int playerId, int value) {
        return db.countTosses(playerId, value);
    }

    public int getHitsPct(int playerId) {
        int zeroes = db.countTosses(playerId, 0);
        int tosses = db.getTotalTosses(playerId);
        return Math.round(100* (tosses - zeroes) / (float) tosses);
    }

    public int getEliminations(int playerId) {
        int eliminations = 0;
        ArrayList<Integer> games = db.getGameIds(playerId);
        for (int gameId : games) {
            if (db.isEliminated(playerId, gameId)) eliminations++;
        }
        return eliminations;
    }

    public int getEliminationsPct(int playerId) {
        int eliminations = 0;
        ArrayList<Integer> games = db.getGameIds(playerId);
        for (int gameId : games) {
            if (db.isEliminated(playerId, gameId)) eliminations++;
        }
        return Math.round(100 * eliminations / (float) games.size());
    }

    public int getExcesses (int playerId) {
        ArrayList<Integer> games = db.getGameIds(playerId);
        int excesses = 0;
        for (int gameId : games) {
            Player p = new Player(db.getTosses(gameId, playerId));
            excesses = excesses + p.countExcesses();
        }
        return excesses;
    }
    public float getExcessesPerGame (int playerId) {
        ArrayList<Integer> games = db.getGameIds(playerId);
        int excesses = 0;
        for (int gameId : games) {
            Player p = new Player(db.getTosses(gameId, playerId));
            excesses = excesses + p.countExcesses();
        }
        return (excesses / (float) games.size());
    }


}
