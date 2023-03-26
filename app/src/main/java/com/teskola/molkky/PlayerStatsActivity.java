package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PlayerStatsActivity extends OptionsActivity {
    private int position;
    private PlayerStats[] players;
    private TextView playerNameTV;
    private TextView pointsTV;
    private TextView gamesTV;
    private TextView winsTV;
    private TextView tossesTV;
    private TextView pptTV;
    private TextView hitsTV;
    private TextView eliminationsTV;
    private TextView excessTV;
    private TextView winningChancesTV;
    private BarChart barChart;
    private ShapeableImageView playerImage;

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
        winningChancesTV = findViewById(R.id.stats_winningChancesTV);

        View showGamesView = findViewById(R.id.gamesTableRow);
        playerImage = findViewById(R.id.titleBar_playerImageView);

        // Read data from intent, savedInstanceState

        if (getIntent().getStringExtra("STATS") != null) {
            String json = getIntent().getStringExtra("STATS");
            players = new Gson().fromJson(json, PlayerStats[].class);
        }
        else {

            // Get stats

            String json = getIntent().getStringExtra("PLAYERS");
            PlayerInfo[] playerInfos = new Gson().fromJson(json, PlayerInfo[].class);
            players = new PlayerStats[playerInfos.length];
            for (int i=0; i < playerInfos.length; i++)
                players[i] = DatabaseHandler.getInstance(this).getPlayerStats(playerInfos[i]);
        }

        if (savedInstanceState != null)
            position = savedInstanceState.getInt("POSITION");
        else
            position = getIntent().getIntExtra("POSITION", 0);

        updateUI();

        // Listeners

        playerImage.setOnClickListener(view -> onImageClicked(players[position].getId(), players[position].getName(), 0, photo -> playerImage.setImageBitmap(photo)));

        previousIB.setOnClickListener(view -> {
            if (position > 0) position--;
            else position = players.length - 1;
            updateUI();
        });
        nextIB.setOnClickListener(view -> {
            if (position < players.length - 1) position++;
            else position = 0;
            updateUI();
        });

        showGamesView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SavedGamesActivity.class);
            intent.putExtra("PLAYER_ID", players[position].getId());
            intent.putExtra("PLAYER_NAME", players[position].getName());
            startActivity(intent);
        });
    }



    private void showBarChart(){

        ArrayList<BarEntry> entries = new ArrayList<>();

        for(int i = 0; i < 13; i++){
            BarEntry barEntry = new BarEntry(i, players[position].getTosses(i));
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

    @SuppressLint("DefaultLocale")
    public  void  updateUI () {
        setImage(playerImage, players[position].getId(), true);

        String winsString = players[position].getWins() + " (" + Math.round(100 * players[position].getWinsPct()) + "%)";
        String hitsString = String.valueOf(Math.round(100 *players[position].getHitsPct()));
        String eliminationsString = players[position].getEliminations() + " (" + Math.round(100 * players[position].getEliminationsPct()) + "%)";

        playerNameTV.setText(players[position].getName());
        pointsTV.setText(String.valueOf(players[position].getPoints()));
        gamesTV.setText(String.valueOf(players[position].getGamesCount()));
        winsTV.setText(winsString);
        tossesTV.setText(String.valueOf(players[position].getTossesCount()));
        pptTV.setText(String.format("%.1f", players[position].getPointsPerToss()));
        hitsTV.setText(hitsString);
        eliminationsTV.setText(eliminationsString);
        excessTV.setText(String.valueOf(players[position].getExcesses()));
        winningChancesTV.setText(String.valueOf(players[position].getWinningChances()));
        showBarChart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("POSITION", position);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            updateUI();
        }
    }
}