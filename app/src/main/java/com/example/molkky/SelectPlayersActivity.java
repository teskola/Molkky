package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Arrays;

public class SelectPlayersActivity extends AppCompatActivity {
    private final ArrayList<Boolean> selected = new ArrayList<>();
    private final ArrayList<PlayerInfo> allPlayers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (getIntent().getStringExtra("SELECTED_PLAYERS") != null) {
            String json = getIntent().getStringExtra("SELECTED_PLAYERS");
            PlayerInfo[] players = new Gson().fromJson(json, PlayerInfo[].class);

            for (PlayerInfo player : players) {
                allPlayers.add(player);
                selected.add(true);
            }
        }

        if (getIntent().getStringExtra("ALL_PLAYERS") != null) {
            String players_json = getIntent().getStringExtra("ALL_PLAYERS");
            PlayerInfo[] players = new Gson().fromJson(players_json, PlayerInfo[].class);
            allPlayers.addAll(Arrays.asList(players));

            String selected_json = getIntent().getStringExtra("SELECTED_BOOLEAN");
            Boolean[] selected_players = new Gson().fromJson(selected_json, Boolean[].class);
            selected.addAll(Arrays.asList(selected_players));

        } else {

            DBHandler dbHandler = DBHandler.getInstance(getApplicationContext());
            ArrayList<PlayerInfo> savedPlayers = dbHandler.getPlayers(allPlayers);
            for (PlayerInfo player : savedPlayers) {
                allPlayers.add(player);
                selected.add(false);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_players);
        TextView titleTV = findViewById(R.id.titleTV);
        ShapeableImageView titleImage = findViewById(R.id.titleBar_playerImageView);
        titleImage.setVisibility(View.GONE);
        titleTV.setText(getString(R.string.choose_player));

        RecyclerView playersContainer = findViewById(R.id.selectPlayersRecyclerView);
        Button okButton = findViewById(R.id.selectPlayersOKButton);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        ListAdapter listAdapter = new ListAdapter(allPlayers, selected, preferences.getBoolean("SHOW_IMAGES", false));
        playersContainer.setAdapter(listAdapter);
        playersContainer.setLayoutManager(new LinearLayoutManager(this));

        okButton.setOnClickListener(view -> {
            ArrayList<PlayerInfo> selectedPlayers = new ArrayList<>();
            for (int i=0; i < selected.size(); i++)
                if (selected.get(i)) selectedPlayers.add(allPlayers.get(i));

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            String json = new Gson().toJson(selectedPlayers);
            intent.putExtra("PLAYERS", json);
            startActivity(intent);
        });

        listAdapter.setOnItemClickListener(new ListAdapter.onItemClickListener() {
            @Override
            public void onSelectClicked(int position) {
                selected.set(position, !selected.get(position));
            }

            @Override
            public void onDeleteClicked(int position) {

            }
        });

    }

    public void openSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        intent.putExtra("ACTIVITY", "select_players");
        String json_players = new Gson().toJson(allPlayers);
        intent.putExtra("ALL_PLAYERS", json_players);
        String json_selected = new Gson().toJson(selected);
        intent.putExtra("SELECTED_BOOLEAN", json_selected);
        startActivity(intent);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.new_game).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {

        case R.id.saved_games:
            Intent intent = new Intent(this, SavedGamesActivity.class);
            startActivity(intent);
            return(true);
        case R.id.stats:
            intent = new Intent(this, AllStatsActivity.class);
            startActivity(intent);
            return(true);
        case R.id.settings:
            openSettings();
            return true;
        case R.id.rules:
            intent = new Intent(this, RulesActivity.class);
            startActivity(intent);
            return true;
    }
        return(super.onOptionsItemSelected(item));
    }


}