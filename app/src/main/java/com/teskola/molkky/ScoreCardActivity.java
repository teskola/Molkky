package com.teskola.molkky;

import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

public class ScoreCardActivity extends OptionsActivity {
    private Player[] players;
    private int position;
    private TextView titleTV, hitsTV, hitsPctTV, avgTV, excessTV, winningChanceTV, eliminationTV;
    private Button allTimeButton;
    private ImageButton previousIB, nextIB;
    private ShapeableImageView playerImage;
    private ViewGroup tossesContainer, titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        hitsTV = findViewById(R.id.scorecard_hitsTV);
        hitsPctTV = findViewById(R.id.scorecard_hitsPctTV);
        avgTV = findViewById(R.id.scorecard_avgTV);
        excessTV = findViewById(R.id.scorecard_excessesTV);
        winningChanceTV = findViewById(R.id.scorecard_winningChancesTV);
        eliminationTV = findViewById(R.id.scorecard_eliminationTV);

        playerImage = findViewById(R.id.titleBar_playerImageView);
        titleTV = findViewById(R.id.titleTV);
        tossesContainer = findViewById(R.id.tossesContainer);
        allTimeButton = findViewById(R.id.allTimeButton);
        titleBar = findViewById(R.id.titleBar);
        previousIB = findViewById(R.id.previousIB);
        nextIB = findViewById(R.id.nextIB);

        allTimeButton.setVisibility(DatabaseHandler.getInstance(this).isNotConnected() ? View.INVISIBLE : View.VISIBLE);
        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);
        titleBar.setBackgroundColor(getResources().getColor(R.color.white));

        // Read data from intent, savedInstanceState

        String json = getIntent().getStringExtra("PLAYERS");
        players = new Gson().fromJson(json, Player[].class);

        if (savedInstanceState != null)
            position = savedInstanceState.getInt("POSITION");
        else
            position = getIntent().getIntExtra("POSITION", 0);

        updateUI();

        // Listeners

        previousIB.setOnClickListener(view -> {
            if (position > 0) position--;
            else position = players.length -1;
            updateUI();
        });
        nextIB.setOnClickListener(view -> {
            if (position < players.length - 1) position++;
            else position = 0;
            updateUI();
        });
        allTimeButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, PlayerStatsActivity.class);
            intent.putExtra("PLAYERS", getIntent().getStringExtra("PLAYERS"));
            intent.putExtra("POSITION", position);
            startActivity(intent);
        });

        playerImage.setOnClickListener(view -> onImageClicked(players[position], 0, photo -> playerImage.setImageBitmap(photo)));
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void updateUI() {
        setImage(playerImage, players[position].getId(), true);

        // Stats table

        String hits = players[position].hits() + "/" + players[position].getTosses().size();
        hitsTV.setText(hits);
        String hitsPct = "(" + players[position].hitsPct() + "%)";
        hitsPctTV.setText(hitsPct);

        avgTV.setText(String.format("%.1f", players[position].mean()));
        excessTV.setText(String.valueOf(players[position].countExcesses()));
        eliminationTV.setText(players[position].isEliminated() ? getString(R.string.yes) : getString(R.string.no));
        winningChanceTV.setText(String.valueOf(players[position].countWinningChances()));

        // Tosses table

        tossesContainer.removeAllViews();
        titleTV.setText(players[position].getName());
        for (int i=0; i < players[position].getTosses().size(); i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.scorecard_tosses, tossesContainer, false);
            TextView toss = view.findViewById(R.id.tossesNumberTV);
            TextView value = view.findViewById(R.id.tossesValueTV);
            TextView pointsTV = view.findViewById(R.id.tossesPointsTV);
            int tossInt = players[position].getToss(i);
            int points = players[position].count(i);
            toss.setText(getString(R.string.scorecard_toss, i+1));
            value.setText(String.valueOf(tossInt));
            pointsTV.setText("(" + points + ")");
            tossesContainer.addView(view);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("POSITION", position);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            setImage(playerImage, players[position].getId(), true);
        }
    }
}