package com.teskola.molkky;

import static java.lang.Math.min;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends FirebaseActivity implements FirebaseManager.LiveGameListener {
    private Game game;
    private ViewGroup switchContainer;
    private LineChart chart;
    private boolean[] checkedArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        switchContainer = findViewById(R.id.switchContainer);
        chart = findViewById(R.id.chart);

        if (getIntent().getStringExtra("json") != null) {
            String json = getIntent().getStringExtra("json");
            game = new Gson().fromJson(json, Game.class);
        }

        if (getIntent().getStringExtra("SPECTATE_MODE") != null) {
            FirebaseManager.getInstance(this).addLiveGameListener(getIntent().getStringExtra("SPECTATE_MODE"), this);
        }

        checkedArray = new boolean[game.getPlayers().size()];
        int setTrue = min(game.getPlayers().size(), 7);
        for (int i=0; i < setTrue; i++) {
            checkedArray[i] = true;
        }
        addSwitchButtons();
        drawChart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseManager.getInstance(this).removeLiveGameListener(getIntent().getStringExtra("SPECTATE_MODE"), this);
    }

    public void addSwitchButtons() {
        for (int i = 0 ; i < game.getPlayers().size(); i ++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.player_switch_layout, switchContainer, false);
            view.setId(100 + i);
            switchContainer.addView(view);
            SwitchCompat switchCompat = findViewById(100 + i);
            switchCompat.setText(game.getPlayer(i).getName());
            if (checkedArray[i]) switchCompat.setChecked(true);

            int index = i;
            switchCompat.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                checkedArray[index] = isChecked;
                drawChart();
            });
        }
    }

    public void drawChart() {
        LineData lineData = new LineData();
        for (int i = 0; i < checkedArray.length; i++) {
            if (checkedArray[i]) {
                List<Entry> entries = new ArrayList<>();
                for (int j=0; j < game.getPlayer(i).getTosses().size(); j++) {
                    entries.add(new Entry(j+1, game.getPlayer(i).count(j)));
                }
                LineDataSet dataSet = new LineDataSet(entries, game.getPlayer(i).getName());
                dataSet.setLineWidth(7f);
                dataSet.setColor(ContextCompat.getColor(getApplicationContext(), colorPalette[i % 7]));
                dataSet.setDrawValues(false);
                dataSet.setCircleColor(ContextCompat.getColor(getApplicationContext(), colorPalette[i % 7]));
                lineData.addDataSet(dataSet);
            }
        }
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.setTouchEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisLeft().setAxisMaximum(50);
        chart.setDescription(null);
        chart.setData(lineData);
        chart.invalidate();
    }

    int[] colorPalette = {
            R.color.teal,
            R.color.saffron,
            R.color.viola,
            R.color.olive,
            R.color.chestnut,
            R.color.pale_orange,
            R.color.robin
    };


    @Override
    public void onLiveGameChange(GameHandler.LiveGame liveGame) {
        if (liveGame.getStarted() != getIntent().getLongExtra("TIMESTAMP", 0))
            FirebaseManager.getInstance(this).removeLiveGameListener(getIntent().getStringExtra("SPECTATE_MODE"), this);
        else {
            List<Toss> tossList = liveGame.getTosses();
            for (Player player : game.getPlayers())
                player.getTosses().clear();
            if (tossList != null) {
                for (Toss toss : tossList) {
                    for (Player player : game.getPlayers()) {
                        if (player.getId().equals(toss.getPid())) {
                            player.addToss(toss.getValue());
                            break;
                        }
                    }
                }
            }
            drawChart();
        }
    }
}


