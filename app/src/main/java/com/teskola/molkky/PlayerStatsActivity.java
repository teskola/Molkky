package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Arrays;
import java.util.List;

public class PlayerStatsActivity extends OptionsActivity implements StatsHandler.StatsReceivedListener {
    private int position;
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
    private StatsHandler statsHandler;

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

        String json = null;
        position = getIntent().getIntExtra("POSITION", 0);
        if (savedInstanceState != null) {
            json = savedInstanceState.getString("STATS");
            position = savedInstanceState.getInt("POSITION");
        }
        else if (getIntent().getStringExtra("STATS") != null) {
            json = getIntent().getStringExtra("STATS");
        }

        if (json != null) {
            PlayerStats[] playersArray = new Gson().fromJson(json, PlayerStats[].class);
            List<PlayerStats> players = new ArrayList<>(Arrays.asList(playersArray));
            statsHandler = new StatsHandler(this, players, this);
            updateUI();
        }
        else {
            json = getIntent().getStringExtra("PLAYERS");
            PlayerInfo[] playerInfos = new Gson().fromJson(json, PlayerInfo[].class);
            statsHandler = new StatsHandler(this, playerInfos, this);
        }

        if (statsHandler.getPlayers().get(position).noData())
            statsHandler.getPlayerStats(position);

        // Listeners

        playerImage.setOnClickListener(view -> onImageClicked(statsHandler.getPlayers().get(position), 0,
                () -> setImage(playerImage, statsHandler.getPlayers().get(position).getId(), true)));

        previousIB.setOnClickListener(view -> {
            if (position > 0) position--;
            else position = statsHandler.getPlayers().size() - 1;
            if (statsHandler.getPlayers().get(position).noData())
                statsHandler.getPlayerStats(position);
            else
                updateUI();
        });
        nextIB.setOnClickListener(view -> {
            if (position < statsHandler.getPlayers().size() - 1) position++;
            else position = 0;
            if (statsHandler.getPlayers().get(position).noData())
                statsHandler.getPlayerStats(position);
            else
                updateUI();
        });

        showGamesView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SavedGamesActivity.class);
            intent.putExtra("PLAYER_ID", statsHandler.getPlayers().get(position).getId());
            intent.putExtra("PLAYER_NAME", statsHandler.getPlayers().get(position).getName());
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if (statsHandler != null)
            statsHandler.close();
    }

    private void showBarChart(){

        ArrayList<BarEntry> entries = new ArrayList<>();

        for(int i = 0; i < 13; i++){
            BarEntry barEntry = new BarEntry(i, statsHandler.getPlayers().get(position).getTosses(i));
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
        setImage(playerImage, statsHandler.getPlayers().get(position).getId(), true);

        String winsString = statsHandler.getPlayers().get(position).getWins() + " (" + Math.round(100 * statsHandler.getPlayers().get(position).getWinsPct()) + "%)";
        String hitsString = String.valueOf(Math.round(100 *statsHandler.getPlayers().get(position).getHitsPct()));
        String eliminationsString = statsHandler.getPlayers().get(position).getEliminations() + " (" + Math.round(100 * statsHandler.getPlayers().get(position).getEliminationsPct()) + "%)";

        playerNameTV.setText(statsHandler.getPlayers().get(position).getName());
        pointsTV.setText(String.valueOf(statsHandler.getPlayers().get(position).getPoints()));
        gamesTV.setText(String.valueOf(statsHandler.getPlayers().get(position).getGamesCount()));
        winsTV.setText(winsString);
        tossesTV.setText(String.valueOf(statsHandler.getPlayers().get(position).getTossesCount()));
        pptTV.setText(String.format("%.1f", statsHandler.getPlayers().get(position).getPointsPerToss()));
        hitsTV.setText(hitsString);
        eliminationsTV.setText(eliminationsString);
        excessTV.setText(String.valueOf(statsHandler.getPlayers().get(position).getExcesses()));
        winningChancesTV.setText(String.valueOf(statsHandler.getPlayers().get(position).getWinningChances()));
        showBarChart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        String json = new Gson().toJson(statsHandler.getPlayers());
        savedInstanceState.putString("STATS", json);
        savedInstanceState.putInt("POSITION", position);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("SHOW_IMAGES")) {
            updateUI();
        }
    }

    @Override
    public void onDataReceived() {
        updateUI();
    }

}