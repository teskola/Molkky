package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerStatsActivity extends AppCompatActivity {
    private int[] playerIds;
    private int position;
    private final PlayerStats playerStats = new PlayerStats(this);

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

        if (getIntent().getIntArrayExtra("PlayerIds") != null) {

            playerIds = getIntent().getIntArrayExtra("PlayerIds");
            position = getIntent().getIntExtra("position", 0);

        }
        updateUI();

        previousIB.setOnClickListener(view -> {
            if (position > 0) position--;
            else position = playerIds.length - 1;
            updateUI();
        });
        nextIB.setOnClickListener(view -> {
            if (position < playerIds.length - 1) position++;
            else position = 0;
            updateUI();
        });

        showGamesView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SavedGamesActivity.class);
            intent.putExtra("PlayerID", playerIds[position]);
            startActivity(intent);
        });

    }

    private void showBarChart(){

        ArrayList<BarEntry> entries = new ArrayList<>();

        for(int i = 0; i < 13; i++){
            BarEntry barEntry = new BarEntry(i, playerStats.getTosses(playerIds[position], i));
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
        playerNameTV.setText(DBHandler.getInstance(getApplicationContext()).getPlayerName(playerIds[position]));
        pointsTV.setText(String.valueOf(playerStats.getPoints(playerIds[position])));
        gamesTV.setText(String.valueOf(playerStats.getGames(playerIds[position])));
        String winsString = playerStats.getWins(playerIds[position]) + " (" + playerStats.getWinsPct(playerIds[position]) + "%)";
        winsTV.setText(winsString);
        tossesTV.setText(String.valueOf(playerStats.getTosses(playerIds[position])));
        pptTV.setText(String.format("%.1f", playerStats.getPointsPerToss(playerIds[position])));
        hitsTV.setText(String.valueOf(playerStats.getHitsPct(playerIds[position])));
        String eliminationsString = playerStats.getEliminations(playerIds[position]) + " (" + playerStats.getEliminationsPct(playerIds[position]) + "%)";
        eliminationsTV.setText(eliminationsString);
        excessTV.setText(String.valueOf(playerStats.getExcesses(playerIds[position])));
        showBarChart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.new_game:
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        case R.id.stats:
            intent = new Intent(this, AllStatsActivity.class);
            startActivity(intent);
            return(true);
        case R.id.saved_games:
            intent = new Intent(this, SavedGamesActivity.class);
            startActivity(intent);
            return(true);
        case R.id.settings:
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return (true);
        case R.id.rules:
            intent = new Intent(this, RulesActivity.class);
            startActivity(intent);
            return true;
    }
        return(super.onOptionsItemSelected(item));
    }


}