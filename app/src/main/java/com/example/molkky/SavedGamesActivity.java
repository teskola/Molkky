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
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class SavedGamesActivity extends AppCompatActivity {

    private ArrayList<GameInfo> games = new ArrayList<>();
    private TextView titleTV;
    private Button showAllBtn;
    private RecyclerView recyclerView;
    private ShapeableImageView playerImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_games);

        titleTV = findViewById(R.id.titleTV);
        showAllBtn = findViewById(R.id.showAllButton);
        playerImageView = findViewById(R.id.titleBar_playerImageView);

        if (getIntent().getExtras() != null) {
            int playerID = getIntent().getIntExtra("PLAYER_ID", 0);
            games = DBHandler.getInstance(getApplicationContext()).getGames(playerID);
            String title = getString(R.string.games) + ": " + DBHandler.getInstance(getApplicationContext()).getPlayerName(playerID);
            titleTV.setText(title);
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
            if (preferences.getBoolean("SHOW_IMAGES", false))
                playerImageView.setVisibility(View.VISIBLE);
            else
                playerImageView.setVisibility(View.GONE);
            showAllBtn.setVisibility(View.VISIBLE);

        } else {
            titleTV.setText(getString(R.string.saved_games));
            playerImageView.setVisibility(View.GONE);
            games = DBHandler.getInstance(getApplicationContext()).getGames();

        }
        recyclerView = findViewById(R.id.savedGamesRW);
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        ListAdapter listAdapter = new ListAdapter(null, null, games, preferences.getBoolean("SHOW_IMAGES", false));
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        showAllBtn.setOnClickListener(view -> {
            showAllGames();
        });

        listAdapter.setOnItemClickListener(new ListAdapter.onItemClickListener() {
            @Override
            public void onSelectClicked(int position) {
                int gameId = games.get(position).getId();
                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                intent.putExtra("gameId", gameId);
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(int position) {

            }
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    public void showAllGames() {
        ArrayList<GameInfo> allGames = DBHandler.getInstance(getApplicationContext()).getGames();
        while (!games.isEmpty()) {
            games.remove(0);
            recyclerView.getAdapter().notifyItemRemoved(0);
        }
        for (GameInfo gameInfo : allGames) {
            games.add(gameInfo);
            recyclerView.getAdapter().notifyItemInserted(games.size() -1);
        }

        titleTV.setText(getString(R.string.saved_games));
        playerImageView.setVisibility(View.GONE);
        showAllBtn.setVisibility(View.INVISIBLE);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("PLAYER_ID", getIntent().getIntExtra("PLAYER_ID", 0));
        intent.putExtra("ACTIVITY", "saved_games");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.saved_games).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.new_game:
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        case R.id.stats:
            intent = new Intent(this, AllStatsActivity.class);
            startActivity(intent);
            return(true);
        case R.id.saved_games:
            intent = new Intent(this, SavedGamesActivity.class);
            startActivity(intent);
            return(true);
        case R.id.settings:
            openSettings();
            return (true);
        case R.id.rules:
            intent = new Intent(this, RulesActivity.class);
            startActivity(intent);
            return true;
    }
        return(super.onOptionsItemSelected(item));
    }

}