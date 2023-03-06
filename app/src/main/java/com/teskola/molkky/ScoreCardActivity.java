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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ScoreCardActivity extends CommonOptions {
    private Game game;
    private int position;
    private TextView titleTV, tossesTV, statsTV;
    private ShapeableImageView playerImage;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private final ImageHandler imageHandler = new ImageHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);

        ConstraintLayout titleBar = findViewById(R.id.titleBar);
        ImageButton previousIB = findViewById(R.id.previousIB);
        ImageButton nextIB = findViewById(R.id.nextIB);
        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);
        playerImage = findViewById(R.id.titleBar_playerImageView);
        titleTV = findViewById(R.id.titleTV);
        tossesTV = findViewById(R.id.allTossesTV);
        statsTV = findViewById(R.id.otherStatsTV);
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
            setImage();
            updateUI();
        });
        nextIB.setOnClickListener(view -> {
            if (position < game.getPlayers().size() - 1) position++;
            else position = 0;
            setImage();
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

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        listener = (sharedPreferences, key) -> {
            if (key.equals("SHOW_IMAGES")) {
                setImage();
                invalidateOptionsMenu();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);
        setImage();
        updateUI();
        playerImage.setOnClickListener(view -> imageHandler.takePicture(ImageHandler.TITLE_BAR));
    }

    protected void onActivityResult(int position, int resultCode, Intent data) {
        super.onActivityResult(position, resultCode, data);
        if (data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageHandler.BitmapToJpg(photo, game.getPlayer(position).getId());
            setImage();
        }
    }

    public void setImage() {
        if (preferences.getBoolean("SHOW_IMAGES", false)) {
            String path = imageHandler.getImagePath(game.getPlayer(position).getId());
            if (path != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                playerImage.setImageBitmap(bitmap);
                playerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            else {
                playerImage.setImageResource(R.drawable.camera);
                playerImage.setScaleType(ImageView.ScaleType.CENTER);
            }
            playerImage.setVisibility(View.VISIBLE);
        }
        else
            playerImage.setVisibility(View.GONE);
    }

    public void updateUI() {

        titleTV.setText(game.getPlayer(position).getName());
        String tosses = buildTossesString();
        String stats = buildStatsString();
        tossesTV.setText(tosses);
        statsTV.setText(stats);
    }

    public StringBuilder fillWithSpaces(StringBuilder line, int length) {
        while(line.length() < length) line.append(" ");
        return line;
    }

    @SuppressLint("DefaultLocale")
    public String buildStatsString() {

        // hits

        int hits = 0;
        for (int i : game.getPlayer(position).getTosses()) {
            if (i != 0) hits++;
        }
        int percentage = Math.round(100 * (float) hits / (float) game.getPlayer(position).getTossesSize());
        StringBuilder hitsSB = new StringBuilder();
        hitsSB.append(getString(R.string.hits)).append(": ").append(hits).append("/").append(game.getPlayer(position).getTossesSize())
                .append(" (").append(percentage).append("%)");
        int length = hitsSB.length();

        // average

        StringBuilder avg = new StringBuilder();
        avg.append(getString(R.string.mean)).append(": ").append(String.format("%.1f", game.getPlayer(position).mean()));
        avg = fillWithSpaces(avg, length);

        // excesses

        StringBuilder exc = new StringBuilder();
        exc.append(getString(R.string.excesses)).append(": ").append(game.getPlayer(position).countExcesses());
        exc = fillWithSpaces(exc, length);

        // elimination

        StringBuilder eli = new StringBuilder();
        eli.append(getString(R.string.elimination)).append(": ").append(game.getPlayer(position).isEliminated() ? getString(R.string.yes) : getString(R.string.no));
        eli = fillWithSpaces(eli, length);

        return avg + "\n" + hitsSB + "\n" + exc + "\n" + eli + "\n";
    }


    public String buildTossesString() {

        ArrayList<Integer> tosses = game.getPlayer(position).getTosses();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tosses.size(); i++) {
            StringBuilder line = new StringBuilder();
            line.append(getString(R.string.toss));
            if (i < 9) line.append("  ");
            else line.append(" ");
            line.append(i+1).append(": ");
            if (tosses.get(i) < 10)
                line.append(" ").append(tosses.get(i));
            else
                line.append(tosses.get(i));
            int points = game.getPlayer(position).count(i);
            if (points < 10) line.append(" ");
            line.append(" (").append(points).append(")");
            sb.append(line).append(" \n");
        }
        return sb.toString();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json = new Gson().toJson(game);
        savedInstanceState.putString("GAME", json);
        savedInstanceState.putInt("POSITION", position);
    }

}