package com.teskola.molkky;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class SavedGamesActivity extends OptionsActivity implements ListAdapter.OnItemClickListener {

    private List<GameInfo> games = new ArrayList<>();
    private TextView titleTV;
    private Button showAllBtn;
    private RecyclerView recyclerView;
    private ShapeableImageView playerImageView;
    private PlayerInfo playerInfo;
    private boolean showAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_games);

        titleTV = findViewById(R.id.titleTV);
        showAllBtn = findViewById(R.id.showAllButton);
        playerImageView = findViewById(R.id.titleBar_playerImageView);

        if (getIntent().getExtras() != null) {
            showAll = false;
            playerInfo = new PlayerInfo();
            playerInfo.setId(getIntent().getStringExtra("PLAYER_ID"));
            games = DatabaseHandler.getInstance(this).getGames(playerInfo.getId());
            playerInfo.setName(DatabaseHandler.getInstance(this).getPlayerName(playerInfo.getId()));
            String title = getString(R.string.games) + ": " + playerInfo.getName();
            titleTV.setText(title);
            if (getPreferences().getBoolean("SHOW_IMAGES", false))
                setImage(playerImageView, playerInfo.getId(), true);
            else
                playerImageView.setVisibility(View.GONE);
            showAllBtn.setVisibility(View.VISIBLE);

        } else {
            showAll = true;
            titleTV.setText(getString(R.string.saved_games));
            playerImageView.setVisibility(View.GONE);
            games = DatabaseHandler.getInstance(this).getGames();

        }
        showAllBtn.setOnClickListener(view -> showAllGames());

        recyclerView = findViewById(R.id.savedGamesRW);
        ListAdapter listAdapter = new ListAdapter(this, games, getPreferences().getBoolean("SHOW_IMAGES", false), this);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        playerImageView.setOnClickListener(view -> onImageClicked(playerInfo.getId(), playerInfo.getName(), 0, photo -> playerImageView.setImageBitmap(photo)));
    }

    public void showAllGames() {
        ArrayList<GameInfo> allGames = DatabaseHandler.getInstance(this).getGames();
        while (!games.isEmpty()) {
            games.remove(0);
            recyclerView.getAdapter().notifyItemRemoved(0);
        }
        for (GameInfo gameInfo : allGames) {
            games.add(gameInfo);
            recyclerView.getAdapter().notifyItemInserted(games.size() - 1);
        }

        titleTV.setText(getString(R.string.saved_games));
        playerImageView.setVisibility(View.GONE);
        showAllBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.saved_games).setVisible(false);
        return true;
    }

    @Override
    public void onSelectClicked(int position) {
        String gameId = games.get(position).getId();
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        intent.putExtra("gameId", gameId);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            if (!showAll)
                setImage(playerImageView, playerInfo.getId(), true);
        }
    }
}