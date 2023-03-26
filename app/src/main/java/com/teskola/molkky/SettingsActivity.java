package com.teskola.molkky;

import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends DatabaseActivity {
    private DatabaseHandler databaseHandler = DatabaseHandler.getInstance(this);

    private SwitchCompat imageSwitch;
    private SwitchCompat useCloudSwitch;
    private TextView gamesTV, playersTV, tossesTV, createdTV, updatedTV;
    private ImageButton infoButton;
    private ViewGroup databaseStats;
    private TextInputEditText editTV;
    private TextInputLayout inputLayout;

    private String created, updated, current;


    private boolean showImages;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imageSwitch = findViewById(R.id.imageSwitch);
        useCloudSwitch = findViewById(R.id.useCloudSwitch);
        editTV = findViewById(R.id.editDBID);
        inputLayout = findViewById(R.id.databaseInputLayout);
        infoButton = findViewById(R.id.infoButton);
        databaseStats = findViewById(R.id.databaseStatsView);

        gamesTV = findViewById(R.id.settings_gamesTV);
        playersTV = findViewById(R.id.settings_playersTV);
        tossesTV = findViewById(R.id.settings_tossesTV);
        createdTV = findViewById(R.id.settings_createdTV);
        updatedTV = findViewById(R.id.settings_updatedTV);



        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        showImages = preferences.getBoolean("SHOW_IMAGES", false);

        editTV.setText(DatabaseHandler.getInstance(this).getDatabaseId());
        editTV.setImeActionLabel(getResources().getString(R.string.confirm), EditorInfo.IME_ACTION_DONE);
        updateDatabaseStats();

        imageSwitch.setChecked(showImages);
        imageSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SHOW_IMAGES", isChecked);
            editor.apply();
        });

        inputLayout.setEndIconVisible(false);
        inputLayout.setStartIconOnClickListener(v -> {
            editTV.setText(DatabaseHandler.getInstance(SettingsActivity.this).getDatabaseId());
        });

        editTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == DatabaseHandler.DATABASE_ID_LENGTH && !s.toString().equals(DatabaseHandler.getInstance(SettingsActivity.this).getDatabaseId())) {
                    String current = s.toString();
                    DatabaseHandler.getInstance(SettingsActivity.this).changeDatabase(current);
                    return;
                }
                inputLayout.setError(null);
                inputLayout.setEndIconVisible(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        infoButton.setOnClickListener(view -> {
          Toast.makeText(this, getResources().getString(R.string.database_instruction), Toast.LENGTH_LONG).show();
        });

    }

    public void updateDatabaseStats() {
        if (DatabaseHandler.getInstance(this).isNotConnected()) {
            databaseStats.setVisibility(View.INVISIBLE);
            return;
        }
        databaseStats.setVisibility(View.VISIBLE);
        int games = databaseHandler.getGamesCount();
        int players = databaseHandler.getPlayersCount();
        int tosses = databaseHandler.getTossesCount();
        created = databaseHandler.getCreated();
        updated = databaseHandler.getUpdated();
        gamesTV.setText(String.valueOf(games));
        playersTV.setText(String.valueOf(players));
        tossesTV.setText(String.valueOf(tosses));
        if (databaseHandler.getUpdated() != null)
            updatedTV.setText(updated);
        if (databaseHandler.getCreated() != null)
            createdTV.setText(created);
    }

    @Override
    public void onDatabaseEvent(DatabaseHandler.Event event) {
        super.onDatabaseEvent(event);
        switch (event) {
            case DATABASE_FOUND:
                inputLayout.setEndIconVisible(true);
                inputLayout.setEndIconDrawable(R.drawable.checkmark);
                editTV.setText(current);
                break;
            case DATABASE_NOT_FOUND:
                inputLayout.setEndIconVisible(true);
                inputLayout.setError(getResources().getString(R.string.database_not_found));
                break;
            case GAME_ADDED:
                updateDatabaseStats();
                break;
            case CREATED_TIMESTAMP_ADDED:
                if (updated == null)
                    updated = databaseHandler.getUpdated();
                updatedTV.setText(updated);
                created = databaseHandler.getCreated();
                createdTV.setText(created);
                break;
        }
    }
}
