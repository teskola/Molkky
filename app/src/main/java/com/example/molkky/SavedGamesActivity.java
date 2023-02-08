package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class SavedGamesActivity extends AppCompatActivity {

    private ArrayList<ListItem> games = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_games);
        DBHandler db = DBHandler.getInstance(this);
        games = db.getGames();
        RecyclerView recyclerView = findViewById(R.id.savedGamesRW);
        ListAdapter listAdapter = new ListAdapter(games, false);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listAdapter.setOnItemClickListener(position -> {
            int gameId = games.get(position).getId();
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.putExtra("gameId", gameId);
            startActivity(intent);

        });

    }
}