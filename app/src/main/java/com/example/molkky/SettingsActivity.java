package com.example.molkky;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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