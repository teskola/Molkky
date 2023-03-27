package com.teskola.molkky;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public abstract class OptionsActivity extends ImagesActivity {

    public void showSpectateDialog () {
        AlertDialog.Builder spectateDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View inputView = inflater.inflate(R.layout.spectate_view, null);
        TextInputLayout inputLayout = inputView.findViewById(R.id.spectateInput);
        EditText editText = inputLayout.getEditText();

        inputLayout.setStartIconOnClickListener(v -> Toast.makeText(OptionsActivity.this, R.string.spectate_instructions, Toast.LENGTH_SHORT).show());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    // TODO tee juttuja
                }
                else
                    inputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        spectateDialog.setTitle(R.string.spectate);
        spectateDialog.setMessage(R.string.set_code);
        spectateDialog.setView(inputView);

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
