package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Stack;

public class GameActivity extends AppCompatActivity {
    public static final int SEEKBAR_DEFAULT_POSITION = 6;
    private Game game;
    private Stack<Integer> undoStack = new Stack<>();

    private SeekBar seekBar;
    private TextView pointsTextView;
    private TextView nameTextView;
    private Button okButton;
    private RecyclerView verticalRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        seekBar = findViewById(R.id.seekBar);
        pointsTextView = findViewById(R.id.pointsTextView);
        nameTextView = findViewById(R.id.playerTextView);
        okButton = findViewById(R.id.okButton);
        verticalRecyclerView = findViewById(R.id.verticalRecyclerView);

        if (getIntent().hasExtra("playersList")) {
            ArrayList<String> playersList = getIntent().getStringArrayListExtra("playersList");
            boolean random = getIntent().getBooleanExtra("random", false);
            int first = getIntent().getIntExtra("first", 0);
            game = new Game(playersList, first, random);
            nameTextView.setText(game.getPlayer(0).getName());
        }

        VerticalAdapter verticalAdapter = new VerticalAdapter(game.getPlayers());
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
                int points = Integer.parseInt(pointsTextView.getText().toString());
                game.getPlayer(0).addToss(points);
                if (game.getPlayer(0).countAll() == 50) {
                    // peli päättynyt
                } else {
                    game.setTurn(1);
                    while (game.getPlayer(0).isDropped()) {
                        game.setTurn(1);
                        updateUI();
                    }
                    if (game.allDropped())  {
                        // peli päättynyt
                    }
                }
                if (!undoStack.empty()) {
                    undoStack.pop();
                }
                updateUI();
            }
        });


    }

    @Override
    public void onBackPressed() {
        int removed_toss_points = game.undo();
        if(removed_toss_points == -1) {
            super.onBackPressed();
        } else {
            if (undoStack.size() > game.getPlayers().size()) {
                undoStack.clear();
            }
            undoStack.push(removed_toss_points);
            updateUI();
        }
    }

    public void updateUI() {

        nameTextView.setText(game.getPlayer(0).getName());
        if (!undoStack.empty()) {
            verticalRecyclerView.getAdapter().notifyDataSetChanged();

            int points = undoStack.peek();
            seekBar.setProgress(points);
            pointsTextView.setText(getResources().getString(R.string.points, points));
            okButton.setEnabled(true);
        } else  {
            verticalRecyclerView.getAdapter().notifyItemRemoved(0);
            verticalRecyclerView.getAdapter().notifyItemInserted(game.getPlayers().size() - 1);

            seekBar.setProgress(SEEKBAR_DEFAULT_POSITION);
            pointsTextView.setText(getResources().getString(R.string.dash));
            okButton.setEnabled(false);
        }
    }
}