package com.teskola.molkky;


import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GameActivity extends OptionsActivity implements ListAdapter.OnItemClickListener, GameHandler.GameListener {
    public static final long SEEKBAR_DEFAULT_POSITION = 6;

    private boolean savedGame = false;
    private boolean spectateMode = false;
    private SeekBar seekBar;
    private TextView pointsTextView;
    private TextView nameTextView;
    private TextView pointsToWinTV;
    private TextView gameIdTV;
    private ViewGroup throwingPinView;
    private ImageView throwingPinImageView;
    private Button okButton;
    private ImageButton chartButton;
    private RecyclerView recyclerView;
    private ConstraintLayout topContainer;
    private ImageView playerImage;

    private GameHandler handler;
    private SharedPreferences saved_state;


    public void loadState(Bundle savedInstanceState) {

        String gameJson = null;

        // Saved state

        if (savedInstanceState != null) {
            gameJson = savedInstanceState.getString("json");
        }
        else if (getIntent().getStringExtra("SAVED_STATE") != null) {
            gameJson = getIntent().getStringExtra("SAVED_STATE");
        }

        if (gameJson != null) {
            String liveId = getIntent().getStringExtra("SPECTATE_MODE");
            spectateMode = liveId != null;
            savedGame = getIntent().getBooleanExtra("SAVED_GAME", false);
            handler = new GameHandler(this, gameJson, liveId, savedGame ? null : this);
            return;
        }

        // New game

        String playersJson = getIntent().getStringExtra("PLAYERS");

        if (getIntent().getStringExtra("SPECTATE_MODE") == null) {
            savedGame = false;
            spectateMode = false;
            boolean random = getIntent().getBooleanExtra("RANDOM", false);
            handler = new GameHandler(this, playersJson, random, this);
            return;
        }

        // New spectator

        spectateMode = true;
        savedGame = false;
        String liveId = getIntent().getStringExtra("SPECTATE_MODE");
        Player[] players = new Gson().fromJson(playersJson, Player[].class);
        ArrayList<Player> playersList = new ArrayList<>();
        Collections.addAll(playersList, players);
        handler = new GameHandler(this, playersList, liveId, this);

    }





    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saved_state = getSharedPreferences("SAVED_STATE", MODE_PRIVATE);
        loadState(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Set up UI

        seekBar = findViewById(R.id.seekBar);
        pointsTextView = findViewById(R.id.pointsTextView);
        nameTextView = findViewById(R.id.nextPlayerTextView);
        pointsToWinTV = findViewById(R.id.pointsToWinTV);
        throwingPinView = findViewById(R.id.throwingPinContainer);
        throwingPinImageView = findViewById(R.id.throwingPinIW);
        okButton = findViewById(R.id.okButton);
        chartButton = findViewById(R.id.chartButton);
        recyclerView = findViewById(R.id.verticalRecyclerView);
        topContainer = findViewById(R.id.topContainer);
        playerImage = findViewById(R.id.game_IW);
        gameIdTV = findViewById(R.id.gameIdTV);

        updateUI();

        // Listeners

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pointsTextView.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pointsTextView.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                okButton.setEnabled(true);
            }
        });

        chartButton.setOnClickListener(view -> openChart());
        pointsTextView.setOnClickListener(view -> {
            if (!handler.gameEnded() && !spectateMode)
                Toast.makeText(this, getString(R.string.seekbar_instruction), Toast.LENGTH_SHORT).show();
        });

        okButton.setOnClickListener(view -> {
            if (!handler.gameEnded()) {
                if (!pointsTextView.getText().equals("-")) {
                    int points = Integer.parseInt(pointsTextView.getText().toString());
                    handler.addToss(points);
                }
            } else
                startNewGame();
        });

        // https://stackoverflow.com/questions/38741787/scroll-textview-inside-recyclerview

        recyclerView.setOnTouchListener((view, motionEvent) -> {
            findViewById(R.id.pointsTV).getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });
    }

    public void clearSavedState() {
        SharedPreferences.Editor editor = saved_state.edit();
        editor.remove("SAVED_STATE");
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if (savedGame || spectateMode || handler.noTosses()) {
            super.onBackPressed();
        } else
            handler.removeToss();
    }


    public void updateUI() {

        //              RecyclerView

        ListAdapter listAdapter;
        if (handler.gameEnded()) {
            listAdapter = new ListAdapter(handler.getPlayers(true), true, false, this);
        } else {
            listAdapter = new ListAdapter(handler.getPlayers(false), false, true, spectateMode ? this : null);
        }
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //              OK-Button

        okButton.setVisibility((spectateMode) ? View.INVISIBLE : View.VISIBLE);
        okButton.setText(handler.gameEnded() ? getString(R.string.new_game) : getString(R.string.ok));
        okButton.setEnabled(handler.gameEnded() || !handler.undoStackIsEmpty());

        //              Chart Button

        chartButton.setVisibility((handler.gameEnded() || spectateMode) ? View.VISIBLE : View.GONE);

        //              Seekbar

        seekBar.setVisibility((handler.gameEnded() || spectateMode) ? View.INVISIBLE : View.VISIBLE);
        seekBar.setProgress((int) handler.getSeekbarPosition());

        //              Points View

        pointsTextView.setVisibility(spectateMode ? View.INVISIBLE : View.VISIBLE);
        if (handler.gameEnded())
            pointsTextView.setText(String.valueOf(handler.getLastToss()));
        else
            pointsTextView.setText(handler.undoStackIsEmpty() ? "-" : String.valueOf(handler.getUndoStackValue()));

        //             Top Container

        nameTextView.setText(handler.getPlayerName());
        setImage(playerImage, handler.current().getId(), false);
        topContainer.setBackgroundResource(handler.gameEnded() ? R.drawable.gold_background : handler.getColor());

        //              Points to win

        if (!handler.gameEnded() && handler.getPointsToWin() > 0) {
            pointsToWinTV.setVisibility(View.VISIBLE);
            pointsToWinTV.setText(getString(R.string.points_to_win, (handler.getPointsToWin())));
        } else
            pointsToWinTV.setVisibility(View.INVISIBLE);

        //              Throwing pin view

        throwingPinView.setVisibility(handler.gameEnded() ? View.VISIBLE : View.INVISIBLE);

        //              GameId
        if (!savedGame)
            gameIdTV.setText(handler.getLiveId());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json = new Gson().toJson(handler.getGame());
        savedInstanceState.putString("json", json);
        savedInstanceState.putBoolean("saved", savedGame);
    }

    public void openChart() {
        Intent intent = new Intent(this, ChartActivity.class);
        String json = new Gson().toJson(handler.getGame());
        intent.putExtra("json", json);
        intent.putExtra("SPECTATE_MODE", getIntent().getStringExtra("SPECTATE_MODE"));
        intent.putExtra("TIMESTAMP", handler.getLiveGameTimestamp());
        startActivity(intent);
    }

    public void openScorecard(int position) {

        Intent intent = new Intent(this, ScoreCardActivity.class);
        String json = new Gson().toJson(handler.getPlayers(handler.gameEnded()));
        intent.putExtra("PLAYERS", json);
        intent.putExtra("POSITION", position);
        intent.putExtra("TIMESTAMP", handler.getLiveGameTimestamp());
        intent.putExtra("SPECTATE_MODE", getIntent().getStringExtra("SPECTATE_MODE"));
        startActivity(intent);
    }

    public void startNewGame() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ArrayList<Player> reversed = new ArrayList<>(handler.getPlayers(true));
        Collections.reverse(reversed);

        String json = new Gson().toJson(reversed);
        intent.putExtra("PLAYERS", json);
        intent.putExtra("NEW_GAME", true);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.stats).setVisible(handler.gameEnded());
        menu.findItem(R.id.saved_games).setVisible(handler.gameEnded());
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        return false;
    }

    @Override
    public void onSelectClicked(int position) {
        openScorecard(position);
    }


    @Override
    public void onTurnChanged(int points, int chanceToWin, boolean undo) {

        Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(0);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemChanged(!undo ? 0 : handler.last());
        recyclerView.getAdapter().notifyItemMoved(!undo ? 0 : handler.last(), !undo ? handler.last() : 0);

        nameTextView.setText(handler.getPlayerName());
        setImage(playerImage, handler.current().getId(), false);
        if (chanceToWin > 0) {
            pointsToWinTV.setText(getString(R.string.points_to_win, (chanceToWin)));
            pointsToWinTV.setVisibility(View.VISIBLE);
        } else
            pointsToWinTV.setVisibility(View.INVISIBLE);
        topContainer.setBackgroundResource(handler.getColor());
        if (points > -1) {
            seekBar.setProgress(points);
            pointsTextView.setText(String.valueOf(points));
            okButton.setEnabled(true);
        } else {
            seekBar.setProgress((int) SEEKBAR_DEFAULT_POSITION);
            pointsTextView.setText("-");
            okButton.setEnabled(false);
        }
    }

    @Override
    public void onGameStatusChanged(boolean gameEnded) {
        invalidateOptionsMenu();
        updateUI();
        if (gameEnded)
            clearSavedState();
    }

    @Override
    public void onNewGameStarted() {
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!savedGame && !spectateMode && !handler.gameEnded()) {
            SharedPreferences.Editor editor = saved_state.edit();
            String json = new Gson().toJson(handler.getGame());
            editor.putString("SAVED_STATE", json);
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearSavedState();
        handler.close();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES"))
            setImage(playerImage, handler.current().getId(), false);
    }
}