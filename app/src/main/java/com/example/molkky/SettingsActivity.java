package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.google.gson.Gson;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat imageSwitch;
    private Button confirmBtn;
    private boolean showImages;
    private SharedPreferences preferences;

    public void returnToPreviousActivity () {
        Intent intent = new Intent();
        switch (getIntent().getStringExtra("ACTIVITY")) {
            case "main":
                intent.setClass(getApplicationContext(), MainActivity.class);
                break;
            case "select_players":
                intent.setClass(getApplicationContext(), SelectPlayersActivity.class);
                break;
            case "scorecard":
                intent.setClass(getApplicationContext(), ScoreCardActivity.class);
                break;
            case "all_stats":
                intent.setClass(getApplicationContext(), AllStatsActivity.class);
                break;
            case "player_stats":
                intent.setClass(getApplicationContext(), PlayerStatsActivity.class);
                break;
            case "saved_games":
                intent.setClass(getApplicationContext(), SavedGamesActivity.class);
                break;
        }
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        imageSwitch = findViewById(R.id.imagesSwitch);
        confirmBtn = findViewById(R.id.confirmButton);

        preferences = getApplicationContext().getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        showImages = preferences.getBoolean("SHOW_IMAGES", false);
        imageSwitch.setChecked(showImages);

        imageSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            showImages = b;
        });

        confirmBtn.setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SHOW_IMAGES", showImages);
            editor.apply();
            returnToPreviousActivity();
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.settings).setVisible(false);
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