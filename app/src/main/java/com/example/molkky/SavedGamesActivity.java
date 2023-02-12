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
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class SavedGamesActivity extends AppCompatActivity {

    private ArrayList<ListItem> games = new ArrayList<>();
    private TextView titleTV;
    private Button showAllBtn;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_games);

        titleTV = findViewById(R.id.titleTV);
        showAllBtn = findViewById(R.id.showAllButton);

        if (getIntent().getExtras() != null) {
            int playerID = getIntent().getIntExtra("PlayerID", 0);
            games = DBHandler.getInstance(getApplicationContext()).getGames(playerID);
            String title = getString(R.string.games) + ": " + DBHandler.getInstance(getApplicationContext()).getPlayerName(playerID);
            titleTV.setText(title);
            showAllBtn.setVisibility(View.VISIBLE);

        } else {
            titleTV.setText(getString(R.string.saved_games));
            games = DBHandler.getInstance(getApplicationContext()).getGames();

        }
        recyclerView = findViewById(R.id.savedGamesRW);
        ListAdapter listAdapter = new ListAdapter(games, false, ListAdapter.SAVED_GAMES_ACTIVITY);
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
        ArrayList<ListItem> allGames = DBHandler.getInstance(getApplicationContext()).getGames();
        while (!games.isEmpty()) {
            games.remove(0);
            recyclerView.getAdapter().notifyItemRemoved(0);
        }
        for (ListItem listItem : allGames) {
            games.add(listItem);
            recyclerView.getAdapter().notifyItemInserted(games.size() -1);
        }

        titleTV.setText(getString(R.string.saved_games));
        showAllBtn.setVisibility(View.INVISIBLE);
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