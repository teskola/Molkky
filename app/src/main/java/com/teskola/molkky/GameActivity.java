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
import java.util.Collections;
import java.util.Objects;

public class GameActivity extends CommonOptions {
    public static final int SEEKBAR_DEFAULT_POSITION = 6;

    private Game game;
    private boolean gameEnded = false;
    private boolean savedGame = false;
    private boolean showImages;

    private SeekBar seekBar;
    private TextView pointsTextView;
    private TextView nameTextView;
    private TextView pointsToWinTV;
    private ViewGroup congratsView;
    private Button okButton;
    private ImageButton chartButton;
    private RecyclerView verticalRecyclerView;
    private ConstraintLayout topContainer;
    private ImageView playerImage;

    private ImageHandler imageHandler = new ImageHandler(this);
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        seekBar = findViewById(R.id.seekBar);
        pointsTextView = findViewById(R.id.pointsTextView);
        nameTextView = findViewById(R.id.nextPlayerTextView);
        pointsToWinTV = findViewById(R.id.pointsToWinTV);
        congratsView = findViewById(R.id.congratsView);
        okButton = findViewById(R.id.okButton);
        chartButton = findViewById(R.id.chartButton);
        verticalRecyclerView = findViewById(R.id.verticalRecyclerView);
        topContainer = findViewById(R.id.topContainer);
        playerImage = findViewById(R.id.game_IW);

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        showImages = preferences.getBoolean("SHOW_IMAGES", false);
        listener = (sharedPreferences, key) -> {
            if (key.equals("SHOW_IMAGES")) {
                showImages = sharedPreferences.getBoolean(key, false);
                setImage();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);

        // Saved game

        if (getIntent().getIntExtra("gameId", 0) != 0) {
            int gameId = getIntent().getIntExtra("gameId", 0);
            game = new Game(gameId, DBHandler.getInstance(getApplicationContext()).getPlayers(gameId));
            gameEnded = true;
            savedGame = true;
            chartButton.setVisibility(View.VISIBLE);
        }

        // New game

        if (getIntent().getStringExtra("json") != null) {
            String json = getIntent().getStringExtra("json");
            Player[] players = new Gson().fromJson(json, Player[].class);
            ArrayList<Player> playersList = new ArrayList<>();
            Collections.addAll(playersList, players);
            boolean random = getIntent().getBooleanExtra("random", false);
            int first = getIntent().getIntExtra("first", 0);
            game = new Game(playersList, first, random);
        }

        if (savedInstanceState != null) {
            String json = savedInstanceState.getString("json");
            game = new Gson().fromJson(json, Game.class);
            gameEnded = savedInstanceState.getBoolean("finished");
            savedGame = savedInstanceState.getBoolean("saved");
        }

        if (savedGame) okButton.setVisibility(View.INVISIBLE);
        nameTextView.setText(game.getPlayer(0).getName());
        if (showImages)
            setImage();

        if (gameEnded) {
            endGame();
            pointsToWinTV.setText(getString(R.string.points_to_win, game.getPlayer(0).getToss(game.getPlayer(0).getTossesSize() - 1)));
        } else {
            VerticalAdapter verticalAdapter = new VerticalAdapter(game.getPlayers(), false, true);
            verticalRecyclerView.setAdapter(verticalAdapter);
            verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        verticalRecyclerView.setOnTouchListener((view, motionEvent) -> {
            findViewById(R.id.pointsTV).getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });

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

        // https://stackoverflow.com/questions/38741787/scroll-textview-inside-recyclerview

        chartButton.setOnClickListener(view -> openChart());

        pointsTextView.setOnClickListener(view -> {
            if (!gameEnded)
                Toast.makeText(this, getString(R.string.instruction), Toast.LENGTH_SHORT).show();
        });

        okButton.setOnClickListener(view -> {
            if (!pointsTextView.getText().equals("-") || savedGame) {
                if (!gameEnded) {
                    Objects.requireNonNull(verticalRecyclerView.getLayoutManager()).scrollToPosition(0);
                    int points = Integer.parseInt(pointsTextView.getText().toString());
                    game.getPlayer(0).addToss(points);
                    if (!game.getPlayer(0).getUndoStack().empty())
                        game.getPlayer(0).getUndoStack().pop();
                    if (game.getPlayer(0).countAll() == 50) {
                        saveGame();
                        endGame();
                    } else {
                        game.setTurn(1);
                        while (game.getPlayer(0).isEliminated()) {
                            game.setTurn(1);
                            updateUI(false);
                        }
                        if (game.allDropped()) {
                            saveGame();
                            endGame();
                        }
                    }
                    if (!gameEnded)
                        updateUI(false);
                } else
                    startNewGame();
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (savedGame || game.getPlayer(game.getPlayers().size() - 1).getTossesSize() == 0) {
            super.onBackPressed();
        } else if (gameEnded) {
            resumeGame();
        } else {
            for (int i = 1; i < game.getPlayers().size(); i++) {
                Player previous = game.getPlayer(game.getPlayers().size() - i);
                Player current = game.getPlayer(0);
                if ((previous.getTossesSize() > current.getTossesSize()) || !previous.isEliminated()) {
                    game.getPlayer(game.getPlayers().size() - i).getUndoStack().push(game.getPlayer(game.getPlayers().size() - i).removeToss());
                    game.setTurn(game.getPlayers().size() - i);
                    updateUI(true);
                    break;
                }
                updateUI(true);
            }
        }
    }

    public void resetSeekBar() {
        seekBar.setProgress(SEEKBAR_DEFAULT_POSITION);
        pointsTextView.setText(getResources().getString(R.string.dash));
        okButton.setEnabled(false);
    }

    public void setImage() {
        if (showImages) {
            String path = imageHandler.getImagePath(game.getPlayer(0).getName());
            if (path != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                playerImage.setImageBitmap(bitmap);
                playerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                playerImage.setVisibility(View.VISIBLE);
            }
            else
                playerImage.setVisibility(View.GONE);
        }
        else
            playerImage.setVisibility(View.GONE);
    }

    public void updateUI(boolean undo) {

        nameTextView.setText(game.getPlayer(0).getName());
        setImage();
        int pointsToWin = game.getPlayer(0).pointsToWin();
        if (pointsToWin > 0) {
            pointsToWinTV.setText(getString(R.string.points_to_win, (pointsToWin)));
            pointsToWinTV.setVisibility(View.VISIBLE);
        } else {
            pointsToWinTV.setVisibility(View.INVISIBLE);
        }
        if (!gameEnded)
            topContainer.setBackgroundResource(selectBackground(game.getPlayer(0), false));

        if (undo) {
            Objects.requireNonNull(verticalRecyclerView.getAdapter()).notifyItemRemoved(game.getPlayers().size() - 1);
            verticalRecyclerView.getAdapter().notifyItemInserted(0);
        } else {
            Objects.requireNonNull(verticalRecyclerView.getAdapter()).notifyItemRemoved(0);
            verticalRecyclerView.getAdapter().notifyItemInserted(game.getPlayers().size() - 1);
            if (!gameEnded)
                resetSeekBar();
        }
        if (!game.getPlayer(0).getUndoStack().empty()) {

            int points = game.getPlayer(0).getUndoStack().peek();
            seekBar.setProgress(points);
            pointsTextView.setText(String.valueOf(points));
            okButton.setEnabled(true);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json = new Gson().toJson(game);
        savedInstanceState.putString("json", json);
        savedInstanceState.putBoolean("finished", gameEnded);
        savedInstanceState.putBoolean("saved", savedGame);
    }

    public void endGame() {
        gameEnded = true;
        invalidateOptionsMenu();
        chartButton.setVisibility(View.VISIBLE);
        pointsToWinTV.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);
        topContainer.setBackgroundResource(R.drawable.gold_background);
        nameTextView.setText(game.getPlayer(0).getName());
        setImage();
        congratsView.setVisibility(View.VISIBLE);
        ArrayList<Player> sortedPlayers = new ArrayList<>(game.getPlayers());
        Collections.sort(sortedPlayers);
        okButton.setText(getString(R.string.start_new_game));
        okButton.setEnabled(true);
        VerticalAdapter verticalAdapter = new VerticalAdapter(sortedPlayers, true, false);
        verticalRecyclerView.setAdapter(verticalAdapter);
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        verticalAdapter.setOnItemClickListener(this::openScorecard);
    }

    public void resumeGame() {
        gameEnded = false;
        invalidateOptionsMenu();
        if (game.getPlayer(0).countAll() == 50) {
            pointsTextView.setText(String.valueOf(game.getPlayer(0).removeToss()));
            pointsToWinTV.setVisibility(View.VISIBLE);
        } else {
            int position = 1;
            while (game.getPlayer(0).getTossesSize() > game.getPlayer(game.getPlayers().size() - position).getTossesSize()) {
                position++;
            }
            game.setTurn(game.getPlayers().size() - position);
            pointsTextView.setText(String.valueOf(game.getPlayer(0).removeToss()));
            game.getPlayer(0).getUndoStack().push(0);
            seekBar.setProgress(0);
            nameTextView.setText(game.getPlayer(0).getName());
            setImage();
            int pointsToWin = game.getPlayer(0).pointsToWin();
            if (pointsToWin > 0) {
                pointsToWinTV.setText(getResources().getString(R.string.points_to_win, pointsToWin));
                pointsToWinTV.setVisibility(View.VISIBLE);
            }

        }
        chartButton.setVisibility(View.INVISIBLE);
        pointsTextView.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        topContainer.setBackgroundResource(selectBackground(game.getPlayer(0), false));
        congratsView.setVisibility(View.INVISIBLE);
        okButton.setText(getString(R.string.ok));
        VerticalAdapter verticalAdapter = new VerticalAdapter(game.getPlayers(), false, true);
        verticalRecyclerView.setAdapter(verticalAdapter);
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DBHandler.getInstance(getApplicationContext()).removeGameFromDatabase(game.getId());

    }

    public void openChart() {
        Intent intent = new Intent(this, ChartActivity.class);
        String json = new Gson().toJson(game);
        intent.putExtra("json", json);
        savedGame = true;
        startActivity(intent);
    }

    public void openScorecard(int position) {
        Intent intent = new Intent(this, ScoreCardActivity.class);
        Game newGame = game;
        Collections.sort(newGame.getPlayers());
        String json = new Gson().toJson(game);
        intent.putExtra("GAME", json);
        intent.putExtra("POSITION", position);
        savedGame = true;
        startActivity(intent);
    }

    public void startNewGame() {
        Intent intent = new Intent(this, MainActivity.class);

        ArrayList<Player> reversed = new ArrayList<>(game.getPlayers());
        Collections.sort(reversed);
        Collections.reverse(reversed);

        String json = new Gson().toJson(reversed);
        intent.putExtra("PLAYERS", json);
        savedGame = true;
        startActivity(intent);
    }

    public void saveGame() {
        new Thread(() -> DBHandler.getInstance(getApplicationContext()).saveGameToDatabase(game)).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.new_game).setVisible(gameEnded);
        menu.findItem(R.id.stats).setVisible(gameEnded);
        menu.findItem(R.id.saved_games).setVisible(gameEnded);
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
            case R.id.images_switch:
                preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("SHOW_IMAGES", !preferences.getBoolean("SHOW_IMAGES", false));
                editor.apply();
                invalidateOptionsMenu();
                return false;
            case R.id.rules:
                intent = new Intent(this, RulesActivity.class);
                break;
        }
        savedGame = true;
        startActivity(intent);
        return false;
    }

    public static int selectBackground(Player player, boolean onlyGray) {
        final int GOLD = R.drawable.gold_background;
        final int PURPLE = R.drawable.purple_background;
        final int GRAY = R.drawable.gray_background;
        final int GREEN = R.drawable.green_background;
        final int GREEN_YELLOW = R.drawable.green_yellow_background;
        final int GREEN_ORANGE = R.drawable.green_orange_background;
        final int YELLOW = R.drawable.yellow_background;
        final int ORANGE = R.drawable.orange_background;
        final int BEIGE = R.drawable.beige_white_background;

        if (player.isEliminated())
            return GRAY;
        if (player.countAll() == 50)
            return GOLD;

        int size = player.getTossesSize();
        if (!onlyGray) {
            int misses = 0;
            if (size > 1 && player.getToss(size - 1) == 0 && player.getToss(size - 2) == 0)
                misses = 2;
            else if (size > 0 && player.getToss(size - 1) == 0)
                misses = 1;
            if (player.excessesTargetPoints(size - 1) == 1)
                return PURPLE;
            if (player.pointsToWin() == 0) {
                if (misses == 2)
                    return ORANGE;
                if (misses == 1)
                    return YELLOW;
                else
                    return BEIGE;
            } else {
                if (misses == 2)
                    return GREEN_ORANGE;
                if (misses == 1)
                    return GREEN_YELLOW;
            }
            return GREEN;
        }
        return BEIGE;
    }

}