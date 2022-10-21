package com.example.molkky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Stack;

public class GameActivity extends AppCompatActivity {
    public static final int SEEKBAR_DEFAULT_POSITION = 6;
    private Game game;
    private Stack<Integer> undoStack = new Stack<>();
    private boolean gameEnded = false;
    private SeekBar seekBar;
    private TextView pointsTextView;
    private TextView nameTextView;
    private TextView pointsToWinTV;
    private TextView congratulationsTextView;
    private ImageView trophyImageView;
    private Button okButton;
    private RecyclerView verticalRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        seekBar = findViewById(R.id.seekBar);
        pointsTextView = findViewById(R.id.pointsTextView);
        nameTextView = findViewById(R.id.nextPlayerTextView);
        pointsToWinTV = findViewById(R.id.pointsToWinTV);
        congratulationsTextView = findViewById(R.id.congratulationsTextView);
        trophyImageView = findViewById(R.id.trophyImageView);
        okButton = findViewById(R.id.okButton);
        verticalRecyclerView = findViewById(R.id.verticalRecyclerView);


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
        }
        nameTextView.setText(game.getPlayer(0).getName());

        if (gameEnded) {
            endGame();
            pointsToWinTV.setText(getString(R.string.points_to_win, game.getPlayer(0).getToss(game.getPlayer(0).getTossesSize() - 1)));
        } else {
            VerticalAdapter verticalAdapter = new VerticalAdapter(game.getPlayers(), false, true);
            verticalRecyclerView.setAdapter(verticalAdapter);
            verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
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

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!gameEnded) {
                    Objects.requireNonNull(verticalRecyclerView.getLayoutManager()).scrollToPosition(0);
                    int points = Integer.parseInt(pointsTextView.getText().toString());
                    game.getPlayer(0).addToss(points);
                    if (game.getPlayer(0).countAll() == 50) {
                        endGame();
                    } else {
                        game.setTurn(1);
                        while (game.getPlayer(0).isDropped()) {
                            game.setTurn(1);
                            updateUI(false);
                        }
                        if (game.allDropped()) {
                            endGame();
                        }

                        if (!undoStack.empty()) {
                            undoStack.pop();
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
        if (game.getPlayer(game.getPlayers().size() - 1).getTossesSize() == 0) {
            super.onBackPressed();
        } else if (gameEnded) {
            resumeGame();
        } else {
            for (int i = 1; i < game.getPlayers().size(); i++) {
                Player previous = game.getPlayer(game.getPlayers().size() - i);
                Player current = game.getPlayer(0);
                if ((previous.getTossesSize() > current.getTossesSize()) || !previous.isDropped()) {
                    undoStack.push(game.getPlayer(game.getPlayers().size() - i).removeToss());
                    game.setTurn(game.getPlayers().size() - i);
                    updateUI(true);
                    break;
                }
                undoStack.push(-1);
                updateUI(true);
            }
        }
    }

    public void resetSeekBar() {
        seekBar.setProgress(SEEKBAR_DEFAULT_POSITION);
        pointsTextView.setText(getResources().getString(R.string.dash));
        okButton.setEnabled(false);
    }

    public void updateUI(boolean undo) {

        nameTextView.setText(game.getPlayer(0).getName());
        int pointsToWin = game.getPlayer(0).pointsToWin();
        if (pointsToWin > 0) {
            pointsToWinTV.setText(getString(R.string.points_to_win, (pointsToWin)));
            pointsToWinTV.setVisibility(View.VISIBLE);
        } else {
            pointsToWinTV.setVisibility(View.INVISIBLE);
        }
        if (!gameEnded)
            nameTextView.setBackgroundResource(VerticalAdapter.MyViewHolder.selectBackground(game.getPlayer(0), false));

        if (undo) {
            verticalRecyclerView.getAdapter().notifyItemRemoved(game.getPlayers().size() - 1);
            verticalRecyclerView.getAdapter().notifyItemInserted(0);
        } else {
            verticalRecyclerView.getAdapter().notifyItemRemoved(0);
            verticalRecyclerView.getAdapter().notifyItemInserted(game.getPlayers().size() - 1);
            if (!gameEnded)
                resetSeekBar();
        }
        if (!undoStack.empty()) {

            int points = undoStack.peek();
            if (points > -1) {

                seekBar.setProgress(points);
                pointsTextView.setText(String.valueOf(points));
                okButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json = new Gson().toJson(game);
        savedInstanceState.putString("json", json);
        savedInstanceState.putBoolean("finished", gameEnded);
    }

    public void endGame() {
        gameEnded = true;
        pointsTextView.setText("");
        pointsToWinTV.setVisibility(View.INVISIBLE);
        trophyImageView.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.INVISIBLE);
        nameTextView.setBackgroundResource(R.drawable.gold_background);
        congratulationsTextView.setText(getString(R.string.congratulations, game.getPlayer(0).getName()));
        congratulationsTextView.setVisibility(View.VISIBLE);
        ArrayList<Player> sortedPlayers = new ArrayList<>(game.getPlayers());
        Collections.sort(sortedPlayers);
        okButton.setText(getString(R.string.new_game));
        okButton.setEnabled(true);
        VerticalAdapter verticalAdapter = new VerticalAdapter(sortedPlayers, true, false);
        verticalRecyclerView.setAdapter(verticalAdapter);
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    public void resumeGame() {
        gameEnded = false;
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
            undoStack.push(0);
            seekBar.setProgress(0);
            nameTextView.setText(game.getPlayer(0).getName());
            int pointsToWin = game.getPlayer(0).pointsToWin();
            if (pointsToWin > 0) {
                pointsToWinTV.setText(getResources().getString(R.string.points_to_win, pointsToWin));
                pointsToWinTV.setVisibility(View.VISIBLE);
            }

        }
        trophyImageView.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        nameTextView.setBackgroundResource(VerticalAdapter.MyViewHolder.selectBackground(game.getPlayer(0), false));
        congratulationsTextView.setVisibility(View.INVISIBLE);
        okButton.setText(getString(R.string.ok));
        VerticalAdapter verticalAdapter = new VerticalAdapter(game.getPlayers(), false, true);
        verticalRecyclerView.setAdapter(verticalAdapter);
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void startNewGame() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        game.clear();
        String json = new Gson().toJson(game.getPlayers());
        intent.putExtra("json", json);
        startActivity(intent);
    }

}