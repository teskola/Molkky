package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class AllStatsActivity extends AppCompatActivity {

    private ArrayList<ListItem> players;
    private int statID = GAMES;
    private TextView statTv;
    private ListAdapter listAdapter;
    private DBHandler db;
    private ImageButton previousIB;
    private ImageButton nextIB;

    public static final int GAMES = 1;
    public static final int WINS = 2;
    public static final int TOTAL_POINTS = 3;
    public static final int TOTAL_TOSSES = 4;
    public static final int POINTS_AVG = 5;
    public static final int HITS_PCT = 6;
    public static final int ELIMINATION_PCT = 7;
    public static final int EXCESSES_PER_GAME = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_stats);
        statTv = findViewById(R.id.allStatsTV);
        previousIB = findViewById(R.id.previousIB);
        nextIB = findViewById(R.id.nextIB);
        db = DBHandler.getInstance(this);
        players = db.getPlayers();
        RecyclerView recyclerView = findViewById(R.id.allStatsRW);
        listAdapter = new ListAdapter(players, true);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (statID < 2) previousIB.setVisibility(View.INVISIBLE);
        if (statID > 6) nextIB.setVisibility(View.INVISIBLE);
        updateUI();

        previousIB.setOnClickListener(view -> {
            if (statID > 1) {
                statID--;
                nextIB.setVisibility(View.VISIBLE);
                updateUI();
            }
            if (statID < 2) {
                previousIB.setVisibility(View.INVISIBLE);
            }
        });

        nextIB.setOnClickListener(view -> {
            if (statID < 8) {
                statID++;
                previousIB.setVisibility(View.VISIBLE);
                updateUI();
            }
            if (statID > 7) {
                nextIB.setVisibility(View.INVISIBLE);
            }
        });

        listAdapter.setOnItemClickListener(position -> {
            Intent intent = new Intent(getApplicationContext(), PlayerStatsActivity.class);
            intent.putExtra("playerId", players.get(position).getId());
            startActivity(intent);
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateUI() {
        if (statID == POINTS_AVG || statID == EXCESSES_PER_GAME) listAdapter.setValueType(ListAdapter.FLOAT);
        else listAdapter.setValueType(ListAdapter.INT);
        if (statID == GAMES) {
            statTv.setText(getString(R.string.games));
            for (ListItem player : players) {
                player.setValueInt(db.getGamesCount(player.getId()));
                player.setValueFloat(0f);
            }
        }
        if (statID == WINS) {
            statTv.setText(getString(R.string.wins));
            for (ListItem player : players) {
                player.setValueInt(db.getWins(player.getId()));
                player.setValueFloat(0f);
            }
        }
        if (statID == TOTAL_POINTS) {
            statTv.setText(getString(R.string.points));
            for (ListItem player : players) {
                player.setValueInt(db.getTotalPoints(player.getId()));
                player.setValueFloat(0f);
            }
        }

        if (statID == TOTAL_TOSSES) {
            statTv.setText(getString(R.string.tosses));
            for (ListItem player : players) {
                player.setValueInt(db.getTotalTosses(player.getId()));
                player.setValueFloat(0f);
            }
        }
        if (statID == POINTS_AVG) {
            statTv.setText(getString(R.string.points_per_toss));
            for (ListItem player : players) {
                float result = db.getTotalPoints(player.getId()) / (float) db.getTotalTosses(player.getId());
                player.setValueFloat(result);
                player.setValueInt(0);
            }
        }

        if (statID == HITS_PCT) {
            statTv.setText(getString(R.string.hits_percentage));
            for (ListItem player : players) {
                int zeroes = db.countTosses(player.getId(), 0);
                int tosses = db.getTotalTosses(player.getId());
                int result = Math.round(100* (tosses - zeroes) / (float) tosses);
                player.setValueFloat(0f);
                player.setValueInt(result);
            }
        }

        if (statID == ELIMINATION_PCT) {
            statTv.setText(getString(R.string.elimination_percentage));
            for (ListItem player : players) {
                int eliminations = 0;
                ArrayList<Integer> games = db.getGames(player.getId());
                for (int gameId : games) {
                    if (db.isEliminated(player.getId(), gameId)) eliminations++;
                }
                int result = Math.round(100 * eliminations / (float) games.size());
                player.setValueFloat(0f);
                player.setValueInt(result);
            }
        }

        if (statID == EXCESSES_PER_GAME) {
            statTv.setText(getString(R.string.excesses_per_game));
            for (ListItem listItem : players) {
                ArrayList<Integer> games = db.getGames(listItem.getId());
                int excesses = 0;
                for (int gameId : games) {
                    Player p = new Player(db.getTosses(gameId, listItem.getId()));
                    excesses = excesses + p.countExcesses();
                }
                listItem.setValueFloat(excesses / (float) games.size());
                listItem.setValueInt(0);
            }
        }
        Collections.sort(players);
        listAdapter.notifyDataSetChanged();
    }
}