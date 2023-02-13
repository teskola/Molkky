package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AllStatsActivity extends AppCompatActivity {

    private ArrayList<PlayerStats> playerStats = new ArrayList<>();
    private int statID;
    private TextView statTv;
    private ListAdapter listAdapter;
    private ImageButton previousIB;
    private ImageButton nextIB;

    public static final int[] stats = {
            R.string.games,
            R.string.wins,
            R.string.points,
            R.string.tosses,
            R.string.points_per_toss,
            R.string.hits_percentage,
            R.string.elimination_percentage,
            R.string.excesses_per_game
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_stats);

        statTv = findViewById(R.id.titleTV);
        previousIB = findViewById(R.id.previousIB);
        nextIB = findViewById(R.id.nextIB);
        ShapeableImageView playerImageView = findViewById(R.id.titleBar_playerImageView);
        playerImageView.setVisibility(View.GONE);
        RecyclerView recyclerView = findViewById(R.id.allStatsRW);


        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);

        statID = getIntent().getIntExtra("STAT_ID", 0);

        ArrayList<PlayerInfo> players = DBHandler.getInstance(getApplicationContext()).getPlayers();
        for (PlayerInfo player : players) {
            playerStats.add(new PlayerStats(player, getApplicationContext()));
        }

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        listAdapter = new ListAdapter(null, playerStats, null, preferences.getBoolean("SHOW_IMAGES", false));
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));




        updateUI();

        previousIB.setOnClickListener(view -> {
            if (statID > 0)
                statID--;
            else
                statID = stats.length - 1;
            updateUI();
        });

        nextIB.setOnClickListener(view -> {
            if (statID < stats.length - 1)
                statID++;
            else
                statID = 0;
            updateUI();

        });
        listAdapter.setOnItemClickListener(new ListAdapter.onItemClickListener() {
            @Override
            public void onSelectClicked(int position) {
                int[] playerIds = new int[playerStats.size()];
                for (int i = 0; i < playerStats.size(); i++) {
                    playerIds[i] = playerStats.get(i).getId();
                }
                Intent intent = new Intent(getApplicationContext(), PlayerStatsActivity.class);
                intent.putExtra("PLAYER_IDS", playerIds);
                intent.putExtra("POSITION", position);
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(int position) {
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "NonConstantResourceId"})
    public void updateUI() {
        statTv.setText(getString(stats[statID]));

        switch (stats[statID]) {
            case R.string.games:
                Collections.sort(playerStats, (b,a) -> Integer.compare(a.getGamesCount(), b.getGamesCount()));
                break;
            case R.string.wins:
                Collections.sort(playerStats, (b,a) -> Integer.compare(a.getWins(), b.getWins()));
                break;
            case R.string.points:
                Collections.sort(playerStats, (b,a) -> Integer.compare(a.getPoints(), b.getPoints()));
                break;
            case R.string.tosses:
                Collections.sort(playerStats, (b,a) -> Integer.compare(a.getTossesCount(), b.getTossesCount()));
                break;
            case R.string.points_per_toss:
                Collections.sort(playerStats, (b,a) -> Float.compare(a.getPointsPerToss(), b.getPointsPerToss()));
                break;
            case R.string.hits_percentage:
                Collections.sort(playerStats, (b,a) -> Float.compare(a.getHitsPct(), b.getHitsPct()));
                break;
            case R.string.elimination_percentage:
                Collections.sort(playerStats, (b,a) -> Float.compare(a.getEliminationsPct(), b.getEliminationsPct()));
                break;
            case R.string.excesses_per_game:
                Collections.sort(playerStats, (b,a) -> Float.compare(a.getExcessesPerGame(), b.getExcessesPerGame()));
                break;
        }
        listAdapter.setStatID(stats[statID]);
        listAdapter.notifyDataSetChanged();
    }

    public void openSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra("ACTIVITY", "all_stats");
        intent.putExtra("STAT_ID", statID);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.stats).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.stats:
                intent = new Intent(this, AllStatsActivity.class);
                startActivity(intent);
                return (true);
            case R.id.saved_games:
                intent = new Intent(this, SavedGamesActivity.class);
                startActivity(intent);
                return (true);
            case R.id.settings:
                openSettings();
                return (true);
            case R.id.rules:
                intent = new Intent(this, RulesActivity.class);
                startActivity(intent);
                return true;
        }
        return (super.onOptionsItemSelected(item));
    }


}