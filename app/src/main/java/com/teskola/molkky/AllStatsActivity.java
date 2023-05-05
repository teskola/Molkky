package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AllStatsActivity extends OptionsActivity implements ListAdapter.OnItemClickListener, StatsHandler.DataChangedListener {

    private List<PlayerStats> playerStats;
    private int statID = 0;
    private RecyclerView recyclerView;
    private TextView statTv;
    private ListAdapter listAdapter;
    private StatsHandler statsHandler;

    public static final int[] stats = {
            R.string.games,
            R.string.wins,
            R.string.points,
            R.string.tosses,
            R.string.points_per_toss,
            R.string.hits_percentage,
            R.string.elimination_percentage,
            R.string.excesses_per_game,
            R.string.winning_chances
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_stats);

        statTv = findViewById(R.id.titleTV);
        ImageButton previousIB = findViewById(R.id.previousIB);
        ImageButton nextIB = findViewById(R.id.nextIB);
        ShapeableImageView playerImageView = findViewById(R.id.titleBar_playerImageView);
        playerImageView.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.allStatsRW);
        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);
        if (savedInstanceState != null) {
            String json = savedInstanceState.getString("PLAYER_STATS");
            PlayerStats[] playerStatsArray = new Gson().fromJson(json, PlayerStats[].class);
            playerStats = Arrays.asList(playerStatsArray);
            statID = savedInstanceState.getInt("STAT_ID");
            statsHandler = new StatsHandler(this, playerStats, this);
        }
        else {
            List<PlayerInfo> players = PlayerHandler.getInstance(this).getPlayers();
            playerStats = new ArrayList<>(players.size());
            for (PlayerInfo player : players) {
                playerStats.add(new PlayerStats(player, 0, null));
            }
            statsHandler = new StatsHandler(this, playerStats, this);
            for (PlayerInfo player : players) statsHandler.getPlayerStats(player);
        }

        createRecyclerView();
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


    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if (statsHandler != null)
            statsHandler.close();
    }

    public void createRecyclerView() {
        listAdapter = new ListAdapter(this, playerStats, getPreferences().getBoolean("SHOW_IMAGES", false), this);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }



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
            case R.string.winning_chances:
                Collections.sort(playerStats, (b,a) -> Integer.compare(a.getWinningChances(), b.getWinningChances()));
                break;
        }
        listAdapter.setStatID(stats[statID]);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.stats).setVisible(false);
        return true;
    }

    @Override
    public void onSelectClicked(int position) {
        Intent intent = new Intent(getApplicationContext(), PlayerStatsActivity.class);

        String json = new Gson().toJson(playerStats);
        intent.putExtra("STATS", json);
        intent.putExtra("POSITION", position);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json = new Gson().toJson(playerStats);
        savedInstanceState.putString("PLAYER_STATS", json);
        savedInstanceState.putInt("STAT_ID", statID);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            createRecyclerView();
            updateUI();
        }
    }

    @Override
    public void onDataChanged() {
        updateUI();
    }
}