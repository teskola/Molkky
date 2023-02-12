package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SelectPlayersActivity extends AppCompatActivity {

    private final ArrayList<ListItem> allPlayers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (getIntent().getStringExtra("json") != null) {
            String json = getIntent().getStringExtra("json");
            ListItem[] players = new Gson().fromJson(json, ListItem[].class);
            for (ListItem player : players) {
                player.setSelected(true);
                allPlayers.add(player);
            }
        }
        DBHandler dbHandler = DBHandler.getInstance(getApplicationContext());
        ArrayList<ListItem> savedPlayers = dbHandler.getPlayers(allPlayers);
        allPlayers.addAll(savedPlayers);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_players);
        TextView titleTV = findViewById(R.id.titleTV);
        titleTV.setText(getString(R.string.choose_player));
        RecyclerView playersContainer = findViewById(R.id.selectPlayersRecyclerView);
        Button okButton = findViewById(R.id.selectPlayersOKButton);
        ListAdapter listAdapter = new ListAdapter(allPlayers, false, ListAdapter.SELECT_PLAYER_VIEW);
        playersContainer.setAdapter(listAdapter);
        playersContainer.setLayoutManager(new LinearLayoutManager(this));

        okButton.setOnClickListener(view -> {
            ArrayList<ListItem> selectedPlayers = new ArrayList<>();
            for (ListItem player : allPlayers) {
                if (player.isSelected()) selectedPlayers.add(player);
            }
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            String json = new Gson().toJson(selectedPlayers);
            intent.putExtra("json", json);
            startActivity(intent);
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        case R.id.rules:
            intent = new Intent(this, RulesActivity.class);
            startActivity(intent);
            return true;
    }
        return(super.onOptionsItemSelected(item));
    }


}