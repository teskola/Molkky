package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SelectPlayersActivity extends ImagesActivity implements ListAdapter.OnItemClickListener {
    private List<Boolean> selected = new ArrayList<>(); // muuta arrayksi
    private List<PlayerInfo> allPlayers = new ArrayList<>();
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            String playersJson = savedInstanceState.getString("ALL_PLAYERS");
            String selectedJson = savedInstanceState.getString("SELECTED_BOOLEAN");
            PlayerInfo[] playersArray = new Gson().fromJson(playersJson, PlayerInfo[].class);
            Boolean[] selectedArray = new Gson().fromJson(selectedJson, Boolean[].class);
            allPlayers = Arrays.asList(playersArray);
            selected = Arrays.asList(selectedArray);

        } else  {

            if (getIntent().getStringExtra("SELECTED_PLAYERS") != null) {
                String selectedJson = getIntent().getStringExtra("SELECTED_PLAYERS");
                PlayerInfo[] selectedPlayers = new Gson().fromJson(selectedJson, PlayerInfo[].class);
                for (PlayerInfo player : selectedPlayers) {
                    allPlayers.add(player);
                    selected.add(true);
                }
                String other = getIntent().getStringExtra("OTHER_PLAYERS");
                PlayerInfo[] otherPlayers = new Gson().fromJson(other, PlayerInfo[].class);
                for (PlayerInfo player : otherPlayers) {
                    allPlayers.add(player);
                    selected.add(false);
                }
            }
        }

        setContentView(R.layout.activity_select_players);
        TextView titleTV = findViewById(R.id.titleTV);
        ShapeableImageView titleImage = findViewById(R.id.titleBar_playerImageView);
        titleImage.setVisibility(View.GONE);
        titleTV.setText(getString(R.string.choose_player));

        recyclerView = findViewById(R.id.selectPlayersRecyclerView);
        createRecyclerView();

    }

    public void createRecyclerView() {
        recyclerView.setAdapter(new ListAdapter(getApplicationContext(), allPlayers, selected, getPreferences().getBoolean("SHOW_IMAGES", false), this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
    public void onSelectClicked(int position) {
        selected.set(position, !selected.get(position));
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(position);
    }

    @Override
    public void onBackPressed() {
        ArrayList<PlayerInfo> selectedPlayers = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++)
            if (selected.get(i)) selectedPlayers.add(allPlayers.get(i));

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        String json = new Gson().toJson(selectedPlayers);
        intent.putExtra("PLAYERS", json);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            createRecyclerView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
