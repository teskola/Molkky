package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

public class SavedGamesActivity extends AppCompatActivity {

    private ArrayList<ListItem> games = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_games);
        if (getIntent().getExtras() != null) {
            int playerID = getIntent().getIntExtra("PlayerID", 0);
            games = DBHandler.getInstance(getApplicationContext()).getGames(playerID);
        } else {
            games = DBHandler.getInstance(getApplicationContext()).getGames();
        }
        RecyclerView recyclerView = findViewById(R.id.savedGamesRW);
        TextView titleTV = findViewById(R.id.titleTV);
        titleTV.setText(getString(R.string.saved_games));
        ListAdapter listAdapter = new ListAdapter(games, false, ListAdapter.SAVED_GAMES_ACTIVITY);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.stats) {
            Intent intent = new Intent(this, AllStatsActivity.class);
            startActivity(intent);
            return (true);
        }
        return(super.onOptionsItemSelected(item));
    }
}