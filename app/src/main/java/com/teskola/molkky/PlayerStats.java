package com.teskola.molkky;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerStats extends PlayerInfo {

    // private LocalDatabaseManager db;
    // private ArrayList<String> games;
    private final HashMap<String, ArrayList<Integer>> tosses;
    private final int wins;
    private int points = -1;
    private int tossesCount= -1;
    private int eliminations = -1;
    private int excesses = -1;

 /*   public PlayerStats(PlayerInfo player, Context context) {
        super(player.getId(), player.getName());
        db = LocalDatabaseManager.getInstance(context);
        this.games = db.getGameIds(getId());
    }*/

    public PlayerStats(PlayerInfo player, int wins, HashMap<String, ArrayList<Integer>> tosses) {
        super(player.getId(), player.getName());
        this.wins = wins;
        this.tosses = tosses;
    }

    public int getTosses(int value) {
        int count = 0;
        for (String key : tosses.keySet()) {
            for (int i : tosses.get(key)) {
                if (i == value)
                    count++;
            }
        }
        return count;
        // return db.countTosses(getId(), value);
    }

    public int getGamesCount() {
        return tosses.size();
        // return games.size();
    }

    public float getPointsPerToss() {
        // if (points == -1) points = db.getTotalPoints(getId());
        // if (tossesCount == -1) tossesCount = db.getTotalTosses(getId());
        return getPoints() / (float) getTossesCount();
    }

    public float getHitsPct() {
        // if (tossesCount == -1) tossesCount = db.getTotalTosses(getId());
        return (getTossesCount() - getTosses(0)) / (float) getTossesCount();
    }

  /*  public void calculateEliminations() {
        int eliminations = 0;
        ArrayList<String> games = db.getGameIds(getId());
        for (String gameId : games) {
            if (db.isEliminated(getId(), gameId)) eliminations++;
        }
        this.eliminations = eliminations;
    }*/

    public float getEliminationsPct() {
       // if (eliminations == -1) calculateEliminations();
        return getEliminations() / (float) tosses.size();
    }

 /*   public void calculateExcesses() {
        int excesses = 0;
        for (String gameId : games) {
            Player p = new Player(db.getTosses(gameId, getId()));
            excesses = excesses + p.countExcesses();
        }
        this.excesses = excesses;
    }*/
    public float getExcessesPerGame () {
       // if (excesses == -1) calculateExcesses();
        return getExcesses() / (float) tosses.size();
    }

    public int getWins() {
        // if (wins == -1) wins = db.getWins(getId());
        return wins;
    }
    public int getExcesses() {
        if (excesses == -1) {
            int count = 0;
            for (String key : tosses.keySet()) {
                Player player = new Player(tosses.get(key));
                count += player.countExcesses();
            }
            excesses = count;
            // calculateExcesses();
        }
        return excesses;
    }

    public float getWinsPct() {
        // if (wins == -1) wins = db.getWins(getId());
        return wins / (float) tosses.size();
    }

    public int getPoints() {
        if (points == -1) {
            int count = 0;
            for (String key : tosses.keySet()) {
                for (int i : tosses.get(key)) {
                    count += i;
                }
            }
            points = count;
        }
        // if (points == -1) points = db.getTotalPoints(getId());
        return points;
    }
    public int getTossesCount() {
        if (tossesCount == -1) {
            int count = 0;
            for (String key : tosses.keySet()) {
                count += tosses.get(key).size();
            }
            tossesCount = count;
        }
        // if (tossesCount == -1) tossesCount = db.getTotalTosses(getId());
        return tossesCount;
    }

    public int getEliminations() {
        if (eliminations == -1) {
            int count = 0;
            for (String key : tosses.keySet()) {
                Player player = new Player(tosses.get(key));
                if (player.isEliminated()) count++;
            }
            eliminations = count;
        }
        // if (eliminations == -1) calculateEliminations();
        return eliminations;
    }

}
