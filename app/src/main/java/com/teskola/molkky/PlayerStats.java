package com.teskola.molkky;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStats extends PlayerInfo {

    private final Map<String, List<Long>> tosses;
    private final int wins;
    private int points = -1;
    private int tossesCount= -1;
    private int eliminations = -1;
    private int excesses = -1;
    private int winningChances = -1;

    public PlayerStats(PlayerInfo player, int wins, Map<String, List<Long>> tosses) {
        super(player.getId(), player.getName());
        this.wins = wins;
        this.tosses = tosses;
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

    public int getGamesCount() {
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
        if (excesses == -1) {
            int count = 0;
            for (String key : tosses.keySet()) {
                Player player = new Player(tosses.get(key));
                count += player.countExcesses();
            }
            excesses = count;
        }
        return excesses;
    }

    public float getWinsPct() {
        return wins / (float) tosses.size();
    }

    public int getPoints() {
        if (points == -1) {
            int count = 0;
            for (String key : tosses.keySet()) {
                for (long i : tosses.get(key)) {
                    count += i;
                }
            }
            points = count;
        }
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
        return eliminations;
    }

    public int getWinningChances() {
        if (winningChances == -1) {
            int count = 0;
            for (String key : tosses.keySet()) {
                Player player = new Player(tosses.get(key));
                count += player.countWinningChances();
            }
            winningChances = count;
        }
        return winningChances;
    }
}
