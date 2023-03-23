package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ScoreCardActivity extends OptionsActivity {
    private Game game;
    private int position;
    private TextView titleTV, hitsTV, hitsPctTV, avgTV, excessTV, winningChanceTV, eliminationTV;
    private ShapeableImageView playerImage;
    private ViewGroup tossesContainer;



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

        ConstraintLayout titleBar = findViewById(R.id.titleBar);
        ImageButton previousIB = findViewById(R.id.previousIB);
        ImageButton nextIB = findViewById(R.id.nextIB);
        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);
        playerImage = findViewById(R.id.titleBar_playerImageView);
        titleTV = findViewById(R.id.titleTV);
        tossesContainer = findViewById(R.id.tossesContainer);
        Button allTimeButton = findViewById(R.id.allTimeButton);
        titleBar.setBackgroundColor(getResources().getColor(R.color.white));


        if (getIntent().getStringExtra("GAME") != null) {
            String json = getIntent().getStringExtra("GAME");
            position = getIntent().getIntExtra("POSITION", 0);
            game = new Gson().fromJson(json, Game.class);
        }

        if (savedInstanceState != null) {
            String json = savedInstanceState.getString("GAME");
            position = savedInstanceState.getInt("POSITION");
            game = new Gson().fromJson(json, Game.class);
        }

        previousIB.setOnClickListener(view -> {
            if (position > 0) position--;
            else position = game.getPlayers().size() -1;
            setImage(playerImage, game.getPlayer(position).getId());
            updateUI();
        });
        nextIB.setOnClickListener(view -> {
            if (position < game.getPlayers().size() - 1) position++;
            else position = 0;
            setImage(playerImage, game.getPlayer(position).getId());
            updateUI();
        });
        allTimeButton.setOnClickListener(view -> {
            String[] playerIds = new String[game.getPlayers().size()];
            for (int i = 0 ; i < game.getPlayers().size(); i++) {
                playerIds[i] = game.getPlayer(i).getId();
            }
            Intent intent = new Intent(getApplicationContext(), PlayerStatsActivity.class);
            intent.putExtra("PLAYER_IDS", playerIds);
            intent.putExtra("POSITION", position);
            startActivity(intent);

        });

        setImage(playerImage, game.getPlayer(position).getId());
        updateUI();
        playerImage.setOnClickListener(view -> onImageClicked(game.getPlayer(position).getId(), 0, new OnImageAdded() {
            @Override
            public void onSuccess(Bitmap photo) {
                playerImage.setImageBitmap(photo);
            }
        }));
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void updateUI() {
        String hits = game.getPlayer(position).hits() + "/" + game.getPlayer(position).getTosses().size();
        hitsTV.setText(hits);
        String hitsPct = "(" + game.getPlayer(position).hitsPct() + "%)";
        hitsPctTV.setText(hitsPct);
        avgTV.setText(String.format("%.1f", game.getPlayer(position).mean()));
        excessTV.setText(String.valueOf(game.getPlayer(position).countExcesses()));
        eliminationTV.setText(game.getPlayer(position).isEliminated() ? getString(R.string.yes) : getString(R.string.no));
        winningChanceTV.setText(String.valueOf(game.getPlayer(position).countWinningChances()));
        tossesContainer.removeAllViews();
        titleTV.setText(game.getPlayer(position).getName());
        for (int i=0; i < game.getPlayer(position).getTosses().size(); i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.scorecard_tosses, tossesContainer, false);
            TextView toss = view.findViewById(R.id.tossesNumberTV);
            TextView value = view.findViewById(R.id.tossesValueTV);
            TextView pointsTV = view.findViewById(R.id.tossesPointsTV);
            int tossInt = game.getPlayer(position).getToss(i);
            int points = game.getPlayer(position).count(i);
            toss.setText(getString(R.string.scorecard_toss, i+1));
            value.setText(String.valueOf(tossInt));
            pointsTV.setText("(" + points + ")");
            tossesContainer.addView(view);

        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json = new Gson().toJson(game);
        savedInstanceState.putString("GAME", json);
        savedInstanceState.putInt("POSITION", position);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            setImage(playerImage, game.getPlayer(position).getId());
        }
    }
}