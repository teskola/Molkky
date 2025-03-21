package com.teskola.molkky;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.Objects;

public abstract class OptionsActivity extends ImagesActivity {

    private AlertDialog dialog;

    public void showSpectateDialog () {
        AlertDialog.Builder spectateDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        try {
            View inputView = inflater.inflate(R.layout.spectate_view, null);
            TextInputLayout inputLayout = inputView.findViewById(R.id.spectateInput);
            EditText editText = inputLayout.getEditText();
            inputLayout.setStartIconOnClickListener(v -> Toast.makeText(OptionsActivity.this, R.string.spectate_instructions, Toast.LENGTH_SHORT).show());
            Objects.requireNonNull(editText).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 4) {
                        String gameId = s.toString();
                        FirebaseManager.getInstance(OptionsActivity.this).fetchLiveGamePlayers(gameId, playerInfos -> {
                            String json = new Gson().toJson(playerInfos);
                            Intent intent = new Intent(OptionsActivity.this, GameActivity.class);
                            intent.putExtra("PLAYERS", json);
                            intent.putExtra("SPECTATE_MODE", gameId);
                            startActivity(intent);
                            dialog.dismiss();
                        }, e -> {
                            if (Objects.equals(e.getMessage(), "game not found")) {
                                inputLayout.setError(getString(R.string.game_not_found));
                            } else
                                Toast.makeText(OptionsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        });

                    }
                    else
                        inputLayout.setError(null);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            spectateDialog.setView(inputView);
        } catch (InflateException exception) {
            System.out.println(exception.getMessage());
        }



        spectateDialog.setTitle(R.string.spectate);
        spectateDialog.setMessage(R.string.set_code);
        spectateDialog.setNegativeButton(R.string.cancel, null);
        dialog = spectateDialog.create();
        dialog.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.spectate:
                if (FirebaseAuth.getInstance().getUid() != null)
                    showSpectateDialog();
                else
                    Toast.makeText(this, getString(R.string.database_connection_failed), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.stats:
                if (PlayerHandler.getInstance(this).noSavedPlayers()) {
                    Toast.makeText(this, getString(R.string.no_saved_players), Toast.LENGTH_SHORT).show();
                    return true;
                }
                startActivity(new Intent(this, AllStatsActivity.class));
                return true;
            case R.id.saved_games:
                if (PlayerHandler.getInstance(this).noSavedPlayers()) {
                    Toast.makeText(this, getString(R.string.no_saved_games), Toast.LENGTH_SHORT).show();
                    return true;
                }
                startActivity(new Intent(this, SavedGamesActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.rules:
                startActivity(new Intent(this, RulesActivity.class));
                return true;
        }
        return false;
    }
}
