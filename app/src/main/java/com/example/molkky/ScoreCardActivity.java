package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ScoreCardActivity extends AppCompatActivity {
    private Game game;
    private int position;
    private TextView titleTV, tossesTV, statsTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        if (getIntent().getStringExtra("json") != null) {
            String json = getIntent().getStringExtra("json");
            position = getIntent().getIntExtra("position", 0);
            game = new Gson().fromJson(json, Game.class);
        }
        ConstraintLayout titleBar = findViewById(R.id.titleBar);
        ImageButton previousIB = findViewById(R.id.previousIB);
        ImageButton nextIB = findViewById(R.id.nextIB);
        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);
        titleTV = findViewById(R.id.titleTV);
        tossesTV = findViewById(R.id.allTossesTV);
        statsTV = findViewById(R.id.otherStatsTV);
        Button allTimeButton = findViewById(R.id.allTimeButton);
        titleBar.setBackgroundColor(getResources().getColor(R.color.white));

        previousIB.setOnClickListener(view -> {
            if (position > 0) position--;
            else position = game.getPlayers().size() -1;
            updateUI();
        });
        nextIB.setOnClickListener(view -> {
            if (position < game.getPlayers().size() - 1) position++;
            else position = 0;
            updateUI();
        });
        allTimeButton.setOnClickListener(view -> {
            int[] playerIds = new int[game.getPlayers().size()];
            for (int i = 0 ; i < game.getPlayers().size(); i++) {
                playerIds[i] = game.getPlayer(i).getId();
            }
            Intent intent = new Intent(getApplicationContext(), PlayerStatsActivity.class);
            intent.putExtra("PlayerIds", playerIds);
            intent.putExtra("position", position);
            startActivity(intent);

        });
        updateUI();
    }

    public void updateUI() {
        titleTV.setText(game.getPlayer(position).getName());
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
        for (int i : game.getPlayer(position).getTosses()) {
            if (i != 0) hits++;
        }
        int percentage = Math.round(100 * (float) hits / (float) game.getPlayer(position).getTossesSize());
        StringBuilder hitsSB = new StringBuilder();
        hitsSB.append(getString(R.string.hits)).append(": ").append(hits).append("/").append(game.getPlayer(position).getTossesSize())
                .append(" (").append(percentage).append("%)");
        int length = hitsSB.length();

        // average

        StringBuilder avg = new StringBuilder();
        avg.append(getString(R.string.mean)).append(": ").append(String.format("%.1f", game.getPlayer(position).mean()));
        avg = fillWithSpaces(avg, length);

        // excesses

        StringBuilder exc = new StringBuilder();
        exc.append(getString(R.string.excesses)).append(": ").append(game.getPlayer(position).countExcesses());
        exc = fillWithSpaces(exc, length);

        // elimination

        StringBuilder eli = new StringBuilder();
        eli.append(getString(R.string.elimination)).append(": ").append(game.getPlayer(position).isEliminated() ? getString(R.string.yes) : getString(R.string.no));
        eli = fillWithSpaces(eli, length);

        return avg + "\n" + hitsSB + "\n" + exc + "\n" + eli + "\n";
    }


    public String buildTossesString() {

        ArrayList<Integer> tosses = game.getPlayer(position).getTosses();
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
            int points = game.getPlayer(position).count(i);
            if (points < 10) line.append(" ");
            line.append(" (").append(points).append(")");
            sb.append(line).append(" \n");
        }
        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.new_game).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {

        case R.id.stats:
            Intent intent = new Intent(this, AllStatsActivity.class);
            startActivity(intent);
            return(true);
        case R.id.saved_games:
            intent = new Intent(this, SavedGamesActivity.class);
            startActivity(intent);
            return(true);
        case R.id.settings:
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return (true);
        case R.id.rules:
            intent = new Intent(this, RulesActivity.class);
            startActivity(intent);
            return true;
    }
        return(super.onOptionsItemSelected(item));
    }

}