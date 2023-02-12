package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class AllStatsActivity extends AppCompatActivity {

    private ArrayList<ListItem> players;
    private int statID = 0;
    private TextView statTv;
    private ListAdapter listAdapter;
    private ImageButton previousIB;
    private ImageButton nextIB;

    private final int[] stats = {
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
        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);

        players = DBHandler.getInstance(getApplicationContext()).getPlayers();

        RecyclerView recyclerView = findViewById(R.id.allStatsRW);
        listAdapter = new ListAdapter(players, true, ListAdapter.STATS_ACTIVITY);
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
                int[] playerIds = new int[players.size()];
                for (int i = 0; i < players.size(); i++) {
                    playerIds[i] = players.get(i).getId();
                }
                Intent intent = new Intent(getApplicationContext(), PlayerStatsActivity.class);
                intent.putExtra("PlayerIds", playerIds);
                intent.putExtra("position", position);
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
        if (stats[statID] == R.string.points_per_toss || stats[statID] == R.string.excesses_per_game)
            listAdapter.setValueType(ListAdapter.FLOAT);
        else listAdapter.setValueType(ListAdapter.INT);

        PlayerStats playerStats = new PlayerStats(this);

        switch (stats[statID]) {
            case R.string.games:
                for (ListItem player : players) {
                    player.setValueInt(playerStats.getGames(player.getId()));
                    player.setValueFloat(0f);
                }
                break;
            case R.string.wins:
                for (ListItem player : players) {
                    player.setValueInt(playerStats.getWins(player.getId()));
                    player.setValueFloat(0f);
                }
                break;
            case R.string.points:
                for (ListItem player : players) {
                    player.setValueInt(playerStats.getPoints(player.getId()));
                    player.setValueFloat(0f);
                }
                break;
            case R.string.tosses:
                for (ListItem player : players) {
                    player.setValueInt(playerStats.getTosses(player.getId()));
                    player.setValueFloat(0f);
                }
                break;
            case R.string.points_per_toss:
                for (ListItem player : players) {
                    player.setValueFloat(playerStats.getPointsPerToss(player.getId()));
                    player.setValueInt(0);
                }
                break;
            case R.string.hits_percentage:
                for (ListItem player : players) {
                    player.setValueFloat(0f);
                    player.setValueInt(playerStats.getHitsPct(player.getId()));
                }
                break;
            case R.string.elimination_percentage:
                for (ListItem player : players) {
                    player.setValueFloat(0f);
                    player.setValueInt(playerStats.getEliminationsPct(player.getId()));
                }
                break;
            case R.string.excesses_per_game:
                for (ListItem player : players) {
                    player.setValueFloat(playerStats.getExcessesPerGame(player.getId()));
                    player.setValueInt(0);
                }
                break;
        }

        Collections.sort(players);
        listAdapter.notifyDataSetChanged();
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
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return (true);
            case R.id.rules:
                intent = new Intent(this, RulesActivity.class);
                startActivity(intent);
                return true;
        }
        return (super.onOptionsItemSelected(item));
    }


}