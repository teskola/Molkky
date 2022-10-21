package com.example.molkky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    private SeekBar seekBar;
    private TextView pointsTextView;
    private TextView nameTextView;
    private TextView pointsToWinTV;
    private Button okButton;
    private RecyclerView verticalRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        seekBar = findViewById(R.id.seekBar);
        pointsTextView = findViewById(R.id.pointsTextView);
        nameTextView = findViewById(R.id.winnerTextView);
        pointsToWinTV = findViewById(R.id.pointsToWinTV);
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
        }

        nameTextView.setText(game.getPlayer(0).getName());

        VerticalAdapter verticalAdapter = new VerticalAdapter(game.getPlayers(), false, true);
        verticalRecyclerView.setAdapter(verticalAdapter);
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                    if (game.allDropped())  {
                        endGame();
                    }
                }
                if (!undoStack.empty()) {
                    undoStack.pop();
                }
                updateUI(false);
            }
        });


    }

    @Override
    public void onBackPressed() {
        if(game.getPlayer(game.getPlayers().size() -1).getTossesSize() == 0) {
            super.onBackPressed();
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
        nameTextView.setBackgroundResource(VerticalAdapter.MyViewHolder.selectBackground(game.getPlayer(0), false));

        if (undo) {

            verticalRecyclerView.getAdapter().notifyItemRemoved(game.getPlayers().size() - 1);
            verticalRecyclerView.getAdapter().notifyItemInserted(0);
        }
     else  {
        verticalRecyclerView.getAdapter().notifyItemRemoved(0);
        verticalRecyclerView.getAdapter().notifyItemInserted(game.getPlayers().size() - 1);
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
    public void onSaveInstanceState (@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
       String json = new Gson().toJson(game);
       savedInstanceState.putString("json", json);
    }

    private void endGame() {

        String json = new Gson().toJson(game);
        Intent intent = new Intent(this, EndActivity.class);
        intent.putExtra("json", json);
        startActivity(intent);
    }

}