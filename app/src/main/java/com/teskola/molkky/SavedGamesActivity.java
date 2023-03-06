package com.teskola.molkky;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class SavedGamesActivity extends CommonOptions {

    private ArrayList<GameInfo> games = new ArrayList<>();
    private TextView titleTV;
    private Button showAllBtn;
    private RecyclerView recyclerView;
    private ShapeableImageView playerImageView;
    private final ImageHandler imageHandler = new ImageHandler(this);
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_games);

        titleTV = findViewById(R.id.titleTV);
        showAllBtn = findViewById(R.id.showAllButton);
        playerImageView = findViewById(R.id.titleBar_playerImageView);
        SharedPreferences preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);

        if (getIntent().getExtras() != null) {
            String playerID = getIntent().getStringExtra("PLAYER_ID");
            games = DBHandler.getInstance(getApplicationContext()).getGames(playerID);
            String name = DBHandler.getInstance(getApplicationContext()).getPlayerName(playerID);
            String title = getString(R.string.games) + ": " + name;
            titleTV.setText(title);
            if (preferences.getBoolean("SHOW_IMAGES", false))
                setImage(playerID);
            else
                playerImageView.setVisibility(View.GONE);
            listener = (sharedPreferences, key) -> {
                if (key.equals("SHOW_IMAGES")) {
                    invalidateMenu();
                    if (sharedPreferences.getBoolean(key, false)) {
                        setImage(playerID);
                    } else
                        playerImageView.setVisibility(View.GONE);
                }
            };
            preferences.registerOnSharedPreferenceChangeListener(listener);
            showAllBtn.setVisibility(View.VISIBLE);

        } else {
            titleTV.setText(getString(R.string.saved_games));
            playerImageView.setVisibility(View.GONE);
            games = DBHandler.getInstance(getApplicationContext()).getGames();

        }
        showAllBtn.setOnClickListener(view -> showAllGames());

        recyclerView = findViewById(R.id.savedGamesRW);
        ListAdapter listAdapter = new ListAdapter(this, games, preferences.getBoolean("SHOW_IMAGES", false), ListAdapter.SAVED_GAMES_ACTIVITY);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listAdapter.setOnItemClickListener(new ListAdapter.onItemClickListener() {
            @Override
            public void onSelectClicked(int position) {
                String gameId = games.get(position).getId();
                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                intent.putExtra("gameId", gameId);
                startActivity(intent);
            }

            @Override
            public void onDeleteClicked(int position) {

            }

            @Override
            public void onImageClicked(int position) {
            }
        });
        playerImageView.setOnClickListener(view -> imageHandler.takePicture(ImageHandler.TITLE_BAR));
    }

    protected void onActivityResult(int position, int resultCode, Intent data) {
        super.onActivityResult(position, resultCode, data);
        if (data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            String id = getIntent().getStringExtra("PLAYER_ID");
            imageHandler.BitmapToJpg(photo, id);
            setImage(id);
        }
    }

    public void setImage(String name) {
        playerImageView.setVisibility(View.VISIBLE);
        ImageHandler imageHandler = new ImageHandler(this);
        String path = imageHandler.getImagePath(name);
        if (path != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            playerImageView.setImageBitmap(bitmap);
            playerImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else {
            playerImageView.setImageResource(R.drawable.camera);
            playerImageView.setScaleType(ImageView.ScaleType.CENTER);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void showAllGames() {
        ArrayList<GameInfo> allGames = DBHandler.getInstance(getApplicationContext()).getGames();
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
}