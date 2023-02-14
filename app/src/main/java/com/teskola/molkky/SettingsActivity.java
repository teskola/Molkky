package com.teskola.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat imageSwitch;
    private Button confirmBtn;
    private boolean showImages;
    private SharedPreferences preferences;

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
            finish();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.settings).setVisible(false);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.new_game:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.stats:
                intent = new Intent(this, AllStatsActivity.class);
                break;
            case R.id.saved_games:
                intent = new Intent(this, SavedGamesActivity.class);
                break;
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.rules:
                intent = new Intent(this, RulesActivity.class);
                break;
        }
        startActivity(intent);
        return false;
    }
}