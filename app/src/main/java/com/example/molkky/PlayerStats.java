package com.example.molkky;

public class PlayerStats extends Player{
    private int games;
    private int wins;
    private int totalPoints;
    private float points_avg;
    private float hitsPct;
    private float eliminationPct;
    private float excessPerGame;

    public PlayerStats(int id, String name) {
        super(id, name);
    }
}
