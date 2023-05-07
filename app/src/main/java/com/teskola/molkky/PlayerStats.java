package com.teskola.molkky;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStats extends PlayerInfo {

    private Map<String, List<Long>> tosses;
    private int wins;

    public PlayerStats(PlayerInfo player, int wins, Map<String, List<Long>> tosses) {
        super(player.getId(), player.getName());
        this.wins = wins;
        this.tosses = tosses;
    }

    public boolean noData() {
        return tosses == null;
    }

    public int getTosses(int value) {
        int count = 0;
        for (String key : tosses.keySet()) {
            for (long i : tosses.get(key)) {
                if (i == value)
                    count++;
            }
        }
        return count;
    }

    public void addTosses (Map<String, List<Long>> tosses) {
        if (this.tosses == null)
            this.tosses = new HashMap<>();
        this.tosses.putAll(tosses);
    }

    public void addWin () {
        this.wins++;
    }

    public int getGamesCount() {
        if (tosses == null)
            return 0;
        return tosses.size();
    }

    public float getPointsPerToss() {
        return getPoints() / (float) getTossesCount();
    }

    public float getHitsPct() {
        return (getTossesCount() - getTosses(0)) / (float) getTossesCount();
    }

    public float getEliminationsPct() {
        return getEliminations() / (float) tosses.size();
    }

    public float getExcessesPerGame () {
        return getExcesses() / (float) tosses.size();
    }

    public int getWins() {
        return wins;
    }
    public int getExcesses() {
        int count = 0;
        for (String key : tosses.keySet()) {
            Player player = new Player(tosses.get(key));
            count += player.countExcesses();
        }
        return count;
    }

    public float getWinsPct() {
        return wins / (float) tosses.size();
    }

    public int getPoints() {

        int count = 0;
        for (String key : tosses.keySet()) {
            for (long i : tosses.get(key)) {
                count += i;
            }
        }
        return count;
    }
    public int getTossesCount() {
        int count = 0;
        for (String key : tosses.keySet()) {
            count += tosses.get(key).size();
        }
        return count;
    }

    public int getEliminations() {
        int count = 0;
        for (String key : tosses.keySet()) {
            Player player = new Player(tosses.get(key));
            if (player.isEliminated()) count++;
        }
        return count;
    }

    public int getWinningChances() {
        int count = 0;
        for (String key : tosses.keySet()) {
            Player player = new Player(tosses.get(key));
            count += player.countWinningChances();
        }
        return count;
    }
}
