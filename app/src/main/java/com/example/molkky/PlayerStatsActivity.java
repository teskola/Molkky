package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayerStatsActivity extends AppCompatActivity {
    private DBHandler dbHandler;
    private int playerID;
    private int games;
    private int wins;
    private int points;
    private int tosses;
    private int zeroes;
    private int eliminations = 0;
    private int excesses = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_stats);
        TextView pointsTV = findViewById(R.id.stats_pointsTV);
        TextView statsTV = findViewById(R.id.stats_otherTV);
        TextView playerNameTV = findViewById(R.id.stats_playerNameTV);
        dbHandler = DBHandler.getInstance(this);

        if (getIntent().getIntExtra("playerId", 0) != 0) {
            playerID = getIntent().getIntExtra("playerId", 0);
            playerNameTV.setText(dbHandler.getPlayerName(playerID));
        }
        wins = dbHandler.getWins(playerID);
        points = dbHandler.getTotalPoints(playerID);
        tosses = dbHandler.getTotalTosses(playerID);
        zeroes = dbHandler.countTosses(playerID, 0);
        ArrayList<Integer> gameIds = dbHandler.getGames(playerID);
        games = gameIds.size();
        for (int gameId : gameIds) {
            if (dbHandler.isEliminated(playerID, gameId)) eliminations++;
        }
        for (int gameId : gameIds) {
            Player p = new Player(dbHandler.getTosses(gameId, playerID));
            excesses = excesses + p.countExcesses();
        }

        String pointsString = pointsString();
        pointsTV.setText(pointsString);
        String statsString = otherStatsString();
        statsTV.setText(statsString);







    }

    public String fillWithSpaces(String line) {
        StringBuilder sb = new StringBuilder(line);
        while(sb.length() < 21) sb.append(" ");
        return sb.toString();
    }

    @SuppressLint("DefaultLocale")
    String otherStatsString () {

        return fillWithSpaces(getString(R.string.games) + ": " + games) + "\n" +
                fillWithSpaces(getString(R.string.wins) + ": " + wins +
                " (" + Math.round(100 * wins / (float) games) + "%)") + "\n" +
                fillWithSpaces(getString(R.string.points) + ": " + points) + "\n" +
                fillWithSpaces(getString(R.string.points_per_toss) + ": " + String.format("%.1f", points / (float) tosses) )+ "\n" +
                fillWithSpaces(getString(R.string.hits_percentage) + ": " + Math.round(100 * (tosses - zeroes) / (float) tosses)) + "\n" +
                fillWithSpaces(getString(R.string.eliminations) + ": " + eliminations +
                " (" + Math.round(100 * eliminations / (float) games) + "%)") + "\n" +
                fillWithSpaces(getString(R.string.excesses) + ": " + excesses);
    }

    String pointsString () {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            int freq = dbHandler.countTosses(playerID, i);
            int percentage = Math.round(100 * freq / (float) tosses);
            sb.append(getString(R.string.points)).append(": ");
            if (i<10) sb.append(" ");
            sb.append(i).append(":");
            if (freq < 100) sb.append(" ");
            if (freq < 10) sb.append(" ");
            sb.append(freq).append(" (");
            if (percentage < 10) sb.append(" ");
            sb.append(percentage).append("%)\n");
        }
        return sb.toString();
    }

}