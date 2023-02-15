package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import java.util.Collections;

public class SelectPlayersActivity extends AppCompatActivity {
    private final ArrayList<Boolean> selected = new ArrayList<>();
    private final ArrayList<PlayerInfo> allPlayers = new ArrayList<>();
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private ImageHandler imageHandler = new ImageHandler(this);


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


        DBHandler dbHandler = DBHandler.getInstance(getApplicationContext());
        ArrayList<PlayerInfo> savedPlayers = dbHandler.getPlayers(allPlayers);
        for (PlayerInfo player : savedPlayers) {
            allPlayers.add(player);
            selected.add(false);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_players);
        TextView titleTV = findViewById(R.id.titleTV);
        ShapeableImageView titleImage = findViewById(R.id.titleBar_playerImageView);
        titleImage.setVisibility(View.GONE);
        titleTV.setText(getString(R.string.choose_player));

        recyclerView = findViewById(R.id.selectPlayersRecyclerView);
        Button okButton = findViewById(R.id.selectPlayersOKButton);

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        createRecyclerView();
        listener = (sharedPreferences, key) -> {
            if (key.equals("SHOW_IMAGES")) {
                createRecyclerView();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);

        okButton.setOnClickListener(view -> {
            ArrayList<PlayerInfo> selectedPlayers = new ArrayList<>();
            for (int i = 0; i < selected.size(); i++)
                if (selected.get(i)) selectedPlayers.add(allPlayers.get(i));

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            String json = new Gson().toJson(selectedPlayers);
            intent.putExtra("PLAYERS", json);
            startActivity(intent);
        });


    }

    public void createRecyclerView() {
        listAdapter = new ListAdapter(getApplicationContext(), allPlayers, selected, preferences.getBoolean("SHOW_IMAGES", false));
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter.setOnItemClickListener(new ListAdapter.onItemClickListener() {
            @Override
            public void onSelectClicked(int position) {
                selected.set(position, !selected.get(position));
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
            imageHandler.BitmapToJpg(photo, allPlayers.get(position).getName());
            listAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json_players = new Gson().toJson(allPlayers);
        String json_selected = new Gson().toJson(selected);
        savedInstanceState.putString("ALL_PLAYERS", json_players);
        savedInstanceState.putString("SELECTED_BOOLEAN", json_selected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.new_game).setVisible(false);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.new_game:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.stats:
                intent = new Intent(this, AllStatsActivity.class);
                break;
            case R.id.saved_games:
                intent = new Intent(this, SavedGamesActivity.class);
                break;
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.rules:
                intent = new Intent(this, RulesActivity.class);
                break;
        }
        startActivity(intent);
        return false;
    }
}