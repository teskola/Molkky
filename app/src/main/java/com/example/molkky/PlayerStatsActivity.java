package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class PlayerStatsActivity extends AppCompatActivity {
    private Player player;
    private TextView playerTV;
    private TextView tossesTV;
    private TextView pointsTV;
    private ViewGroup tossesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_stats);

        if (getIntent().getStringExtra("json") != null) {
            String json = getIntent().getStringExtra("json");
            player = new Gson().fromJson(json, Player.class);
        }
        playerTV = findViewById(R.id.playerNameTV);
        tossesTV = findViewById(R.id.stats_allTossesTV);
        tossesContainer = findViewById(R.id.tossesContainer);
        playerTV.setText(player.getName());

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ArrayList<Integer> tosses = player.getTosses();
        for (int i = 0; i < tosses.size(); i++) {
            View view = inflater.inflate(R.layout.tosses_text, tossesContainer, false);
            view.setId(i);
            tossesContainer.addView(view);
            tossesTV = findViewById(i);
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.toss));
            if (i < 9) sb.append("  ");
            else sb.append(" ");
            sb.append(i+1).append(": ");
            if (tosses.get(i) < 10)
                sb.append(" ").append(tosses.get(i));
            else
                sb.append(tosses.get(i));
            sb.append(" (");
            sb.append(player.count(i)).append(")");
            tossesTV.setText(sb.toString());
        }


    }
    public String buildTossesString() {
        ArrayList<Integer> tosses = player.getTosses();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tosses.size(); i++) {
            sb.append(getString(R.string.toss));
            if (i < 9) sb.append("  ");
            else sb.append(" ");
            sb.append(i+1).append(": ");
            if (tosses.get(i) < 10)
                sb.append(" ").append(tosses.get(i));
            else
                sb.append(tosses.get(i));
            sb.append(" (");
            sb.append(player.count(i));
            sb.append(")\n");
        }
        return sb.toString();
    }
}