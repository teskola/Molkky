package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ScoreCardActivity extends AppCompatActivity {
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        if (getIntent().getStringExtra("json") != null) {
            String json = getIntent().getStringExtra("json");
            player = new Gson().fromJson(json, Player.class);
        }
        TextView playerTV = findViewById(R.id.playerNameTV);
        TextView tossesTV = findViewById(R.id.allTossesTV);
        TextView statsTV = findViewById(R.id.otherStatsTV);
        playerTV.setText(player.getName());
        String tosses = buildTossesString();
        String stats = buildStatsString();
        tossesTV.setText(tosses);
        statsTV.setText(stats);
    }

    public StringBuilder fillWithSpaces(StringBuilder line, int length) {
        while(line.length() < length) line.append(" ");
        return line;
    }

    @SuppressLint("DefaultLocale")
    public String buildStatsString() {

        // hits

        int hits = 0;
        for (int i : player.getTosses()) {
            if (i != 0) hits++;
        }
        int percentage = Math.round(100 * (float) hits / (float) player.getTossesSize());
        StringBuilder hitsSB = new StringBuilder();
        hitsSB.append(getString(R.string.hits)).append(": ").append(hits).append("/").append(player.getTossesSize())
                .append(" (").append(percentage).append("%)");
        int length = hitsSB.length();

        // average

        StringBuilder avg = new StringBuilder();
        avg.append(getString(R.string.mean)).append(": ").append(String.format("%.1f", player.mean()));
        avg = fillWithSpaces(avg, length);

        // excesses

        StringBuilder exc = new StringBuilder();
        exc.append(getString(R.string.excesses)).append(": ").append(player.countExcesses());
        exc = fillWithSpaces(exc, length);

        // elimination

        StringBuilder eli = new StringBuilder();
        eli.append(getString(R.string.elimination)).append(": ").append(player.isEliminated() ? getString(R.string.yes) : getString(R.string.no));
        eli = fillWithSpaces(eli, length);

        StringBuilder sb = new StringBuilder();
        sb.append(avg).append("\n").append(hitsSB).append("\n").append(exc).append("\n").append(eli).append("\n");
        return sb.toString();
    }


    public String buildTossesString() {

        ArrayList<Integer> tosses = player.getTosses();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tosses.size(); i++) {
            StringBuilder line = new StringBuilder();
            line.append(getString(R.string.toss));
            if (i < 9) line.append("  ");
            else line.append(" ");
            line.append(i+1).append(": ");
            if (tosses.get(i) < 10)
                line.append(" ").append(tosses.get(i));
            else
                line.append(tosses.get(i));
            int points = player.count(i);
            if (points < 10) line.append(" ");
            line.append(" (").append(points).append(")");
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}