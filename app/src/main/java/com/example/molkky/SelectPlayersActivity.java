package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SelectPlayersActivity extends AppCompatActivity {

    private final ArrayList<Player> allPlayers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (getIntent().getStringExtra("json") != null) {
            String json = getIntent().getStringExtra("json");
            Player[] players = new Gson().fromJson(json, Player[].class);
            for (Player p : players) {
                p.setSelected(true);
                allPlayers.add(p);
            }
        }
        DBHandler dbHandler = DBHandler.getInstance(this);
        ArrayList<Player> savedPlayers = dbHandler.getPlayers(allPlayers);
        allPlayers.addAll(savedPlayers);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_players);
        RecyclerView playersContainer = findViewById(R.id.selectPlayersRecyclerView);
        Button okButton = findViewById(R.id.selectPlayersOKButton);
        AddPlayersAdapter myAdapter = new AddPlayersAdapter(allPlayers, AddPlayersAdapter.SELECT_PLAYER_VIEW);
        playersContainer.setAdapter(myAdapter);
        playersContainer.setLayoutManager(new LinearLayoutManager(this));

        okButton.setOnClickListener(view -> {
            ArrayList<Player> selectedPlayers = new ArrayList<>();
            for (Player player : allPlayers) {
                if (player.isSelected()) selectedPlayers.add(player);
            }
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            String json = new Gson().toJson(selectedPlayers);
            intent.putExtra("json", json);
            startActivity(intent);
        });


    }


}