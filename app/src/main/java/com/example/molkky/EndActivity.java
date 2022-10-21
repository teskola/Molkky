package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

public class EndActivity extends AppCompatActivity {

    private Game game;
    private TextView winnerTextView;
    private RecyclerView recyclerView;
    private Button newGameButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        if (getIntent().getStringExtra("json") != null) {
            String json = getIntent().getStringExtra("json");
            game = new Gson().fromJson(json, Game.class);
        }

        if (savedInstanceState != null) {
            String json = savedInstanceState.getString("json");
            game = new Gson().fromJson(json, Game.class);
        }
        winnerTextView = findViewById(R.id.winnerTextView);
        recyclerView = findViewById(R.id.verticalRecyclerView);
        newGameButton = findViewById(R.id.newGameButton);

        winnerTextView.setText(game.getPlayer(0).getName());
        ArrayList<Player> losers = new ArrayList<>(game.getPlayers());
        losers.remove(0);
        Collections.sort(losers);

        VerticalAdapter verticalAdapter = new VerticalAdapter(losers, true, false);
        recyclerView.setAdapter(verticalAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                game.clear();
                String json = new Gson().toJson(game.getPlayers());
                intent.putExtra("json", json);
                startActivity(intent);
            }
        });



    }
}