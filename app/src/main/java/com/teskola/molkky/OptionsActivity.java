package com.teskola.molkky;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.view.menu.MenuBuilder;

public abstract class OptionsActivity extends ImagesActivity {

    public void showSpectateDialog () {
        AlertDialog.Builder spectateDialog = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        spectateDialog.setTitle(R.string.spectate);
        spectateDialog.setMessage(R.string.spectate_code_instruction);
        spectateDialog.setView(R.layout.spectate_view);
        spectateDialog.setPositiveButton(R.string.ok, (dialog, which) -> {

        });
        spectateDialog.setNegativeButton(R.string.cancel, (dialog, which) -> {

        });
        spectateDialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.spectate:
                showSpectateDialog();
                return true;
            case R.id.stats:
                if (DatabaseHandler.getInstance(this).noPlayers()) {
                    Toast.makeText(this, getString(R.string.no_saved_players), Toast.LENGTH_SHORT).show();
                    return true;
                }
                intent = new Intent(this, AllStatsActivity.class);
                break;
            case R.id.saved_games:
                if (DatabaseHandler.getInstance(this).getGamesCount() == 0) {
                    Toast.makeText(this, getString(R.string.no_saved_games), Toast.LENGTH_SHORT).show();
                    return true;
                }
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
