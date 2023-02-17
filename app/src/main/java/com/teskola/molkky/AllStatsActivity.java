package com.teskola.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Collections;

public class AllStatsActivity extends BaseActivity {

    private ArrayList<PlayerStats> playerStats = new ArrayList<>();
    private int statID;
    private RecyclerView recyclerView;
    private TextView statTv;
    private ListAdapter listAdapter;
    private ImageButton previousIB;
    private ImageButton nextIB;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private ImageHandler imageHandler = new ImageHandler(this);

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
        recyclerView = findViewById(R.id.allStatsRW);
        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);
        statID = getIntent().getIntExtra("STAT_ID", 0);

        ArrayList<PlayerInfo> players = DBHandler.getInstance(getApplicationContext()).getPlayers();
        for (PlayerInfo player : players) {
            playerStats.add(new PlayerStats(player, getApplicationContext()));
        }

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        listener = (sharedPreferences, key) -> {
            if (key.equals("SHOW_IMAGES")) {
                createRecyclerView();
                updateUI();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);



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

    public void createRecyclerView() {
        listAdapter = new ListAdapter(this, null, playerStats, null, preferences.getBoolean("SHOW_IMAGES", false));
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

            @Override
            public void onImageClicked(int position) {
                imageHandler.takePicture(position);
            }
        });
    }

    protected void onActivityResult(int position, int resultCode, Intent data) {
        super.onActivityResult(position, resultCode, data);
        if (data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageHandler.BitmapToJpg(photo, playerStats.get(position).getName());
            listAdapter.notifyItemChanged(position);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.stats).setVisible(false);
        return true;
    }

}