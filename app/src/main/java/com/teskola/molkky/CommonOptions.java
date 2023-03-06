package com.teskola.molkky;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public abstract class CommonOptions extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        boolean showImages = preferences.getBoolean("SHOW_IMAGES", false);
        MenuItem imageSwitch = menu.findItem(R.id.images_switch);
        if (showImages) imageSwitch.setTitle(R.string.hide_images);
        else imageSwitch.setTitle(R.string.show_images);

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
            case R.id.images_switch:
                preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("SHOW_IMAGES", !preferences.getBoolean("SHOW_IMAGES", false));
                editor.apply();
                invalidateOptionsMenu();
                return false;
            case R.id.rules:
                intent = new Intent(this, RulesActivity.class);
                break;
        }
        startActivity(intent);
        return false;
    }

}
