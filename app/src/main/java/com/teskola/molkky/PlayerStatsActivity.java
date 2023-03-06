package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class PlayerStatsActivity extends CommonOptions {
    private String[] playerIds;
    private int position;
    private PlayerStats playerStats;
    private TextView playerNameTV;
    private TextView pointsTV;
    private TextView gamesTV;
    private TextView winsTV;
    private TextView tossesTV;
    private TextView pptTV;
    private TextView hitsTV;
    private TextView eliminationsTV;
    private TextView excessTV;
    private BarChart barChart;
    private ShapeableImageView playerImage;
    private SharedPreferences preferences;
    private final ImageHandler imageHandler = new ImageHandler(this);


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_stats);
        ConstraintLayout titleBar = findViewById(R.id.titleBar);
        titleBar.setBackgroundColor(getResources().getColor(R.color.white));

        ImageButton previousIB = findViewById(R.id.previousIB);
        ImageButton nextIB = findViewById(R.id.nextIB);
        previousIB.setVisibility(View.VISIBLE);
        nextIB.setVisibility(View.VISIBLE);
        barChart = findViewById(R.id.barChartView);

        playerNameTV = findViewById(R.id.titleTV);
        pointsTV = findViewById(R.id.stats_pointsTV);
        gamesTV = findViewById(R.id.stats_gamesTV);
        winsTV = findViewById(R.id.stats_winsTV);
        tossesTV = findViewById(R.id.stats_TossesTV);
        pptTV = findViewById(R.id.stats_pptTV);
        hitsTV = findViewById(R.id.stats_hitsTV);
        eliminationsTV = findViewById(R.id.stats_elimTV);
        excessTV = findViewById(R.id.stats_excessesTV);
        View showGamesView = findViewById(R.id.gamesTableRow);
        playerImage = findViewById(R.id.titleBar_playerImageView);

        if (getIntent().getStringArrayExtra("PLAYER_IDS") != null) {

            playerIds = getIntent().getStringArrayExtra("PLAYER_IDS");
            position = getIntent().getIntExtra("POSITION", 0);

        }

        if (savedInstanceState != null) {
            playerIds = savedInstanceState.getStringArray("PLAYER_IDS");
            position = savedInstanceState.getInt("POSITION");
        }

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
            if (key.equals("SHOW_IMAGES")) {
                invalidateOptionsMenu();
                updateUI();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);

        playerImage.setOnClickListener(view -> imageHandler.takePicture(ImageHandler.TITLE_BAR));

        previousIB.setOnClickListener(view -> {
            if (position > 0) position--;
            else position = playerIds.length - 1;
            getPlayerData();
            updateUI();
        });
        nextIB.setOnClickListener(view -> {
            if (position < playerIds.length - 1) position++;
            else position = 0;
            getPlayerData();
            updateUI();
        });

        showGamesView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SavedGamesActivity.class);
            intent.putExtra("PLAYER_ID", playerIds[position]);
            startActivity(intent);
        });


        getPlayerData();
        updateUI();


    }

    protected void onActivityResult(int position, int resultCode, Intent data) {
        super.onActivityResult(position, resultCode, data);
        if (data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageHandler.BitmapToJpg(photo, playerStats.getName());
            updateImage();
        }
    }

    private void showBarChart(){

        ArrayList<BarEntry> entries = new ArrayList<>();

        for(int i = 0; i < 13; i++){
            BarEntry barEntry = new BarEntry(i, playerStats.getTosses(i));
            entries.add(barEntry);
        }

        BarDataSet barDataSet = new BarDataSet(entries, null);
        barDataSet.setDrawValues(false);
        barDataSet.setColor(ContextCompat.getColor(getApplicationContext(), R.color.teal));

        BarData data = new BarData(barDataSet);

        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisLeft().setDrawAxisLine(false);

        barChart.getAxisRight().setDrawLabels(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0);
        barChart.getXAxis().setAxisMinimum(-0.5f);
        barChart.getXAxis().setAxisMaximum(12.5f);



        barChart.setTouchEnabled(false);
        barChart.setDescription(null);
        barChart.setData(data);
        barChart.invalidate();


    }

    public void getPlayerData() {
        String name = DBHandler.getInstance(getApplicationContext()).getPlayerName(playerIds[position]);
        PlayerInfo player = new PlayerInfo(playerIds[position], name);
        playerStats = new PlayerStats(player, getApplicationContext());
    }

    public void updateImage () {
        if (preferences.getBoolean("SHOW_IMAGES", false)) {
            String path = imageHandler.getImagePath(playerStats.getName());
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

    @SuppressLint("DefaultLocale")
    public  void  updateUI () {
        updateImage();
        playerNameTV.setText(playerStats.getName());
        pointsTV.setText(String.valueOf(playerStats.getPoints()));
        gamesTV.setText(String.valueOf(playerStats.getGamesCount()));
        String winsString = playerStats.getWins() + " (" + Math.round(100 * playerStats.getWinsPct()) + "%)";
        winsTV.setText(winsString);
        tossesTV.setText(String.valueOf(playerStats.getTossesCount()));
        pptTV.setText(String.format("%.1f", playerStats.getPointsPerToss()));
        hitsTV.setText(String.valueOf(Math.round(100 *playerStats.getHitsPct())));
        String eliminationsString = playerStats.getEliminations() + " (" + Math.round(100 * playerStats.getEliminationsPct()) + "%)";
        eliminationsTV.setText(eliminationsString);
        excessTV.setText(String.valueOf(playerStats.getExcesses()));
        showBarChart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArray("PLAYER_IDS", playerIds);
        savedInstanceState.putInt("POSITION", position);
    }
}