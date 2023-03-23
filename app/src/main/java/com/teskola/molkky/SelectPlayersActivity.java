package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class SelectPlayersActivity extends ImagesActivity implements ListAdapter.OnItemClickListener {
    private final ArrayList<Boolean> selected = new ArrayList<>();
    private final ArrayList<PlayerInfo> allPlayers = new ArrayList<>();
    private RecyclerView recyclerView;


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

        ArrayList<PlayerInfo> savedPlayers = (ArrayList<PlayerInfo>) DatabaseHandler.getInstance(this).getPlayers(allPlayers);
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
        createRecyclerView();
        okButton.setOnClickListener(view -> {
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
        });


    }

    public void createRecyclerView() {
        recyclerView.setAdapter(new ListAdapter(getApplicationContext(), allPlayers, selected, getPreferences().getBoolean("SHOW_IMAGES", false), this));
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
        recyclerView.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            createRecyclerView();
        }
    }
}
