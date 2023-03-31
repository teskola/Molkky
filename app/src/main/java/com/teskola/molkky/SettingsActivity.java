package com.teskola.molkky;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Stack;

public class SettingsActivity extends DatabaseActivity {
    private DatabaseHandler databaseHandler = DatabaseHandler.getInstance(this);

    private SwitchCompat imageSwitch;
    private SwitchCompat useCloudSwitch;
    private TextView gamesTV, playersTV, tossesTV, createdTV, updatedTV;
    private ViewGroup databaseStats;
    private EditText editTV;
    private TextInputLayout inputLayout;
    private String created, updated;
    private boolean showImages, useCloud;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imageSwitch = findViewById(R.id.imageSwitch);
        useCloudSwitch = findViewById(R.id.useCloudSwitch);
        inputLayout = findViewById(R.id.databaseInputLayout);
        editTV = inputLayout.getEditText();
        databaseStats = findViewById(R.id.databaseStatsView);

        gamesTV = findViewById(R.id.settings_gamesTV);
        playersTV = findViewById(R.id.settings_playersTV);
        tossesTV = findViewById(R.id.settings_tossesTV);
        createdTV = findViewById(R.id.settings_createdTV);
        updatedTV = findViewById(R.id.settings_updatedTV);

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        showImages = preferences.getBoolean("SHOW_IMAGES", false);
        useCloud = preferences.getBoolean("USE_CLOUD", true);
        editTV.setText(DatabaseHandler.getInstance(this).getDatabaseId());
        editTV.setSelection(editTV.getText().length());
        updateDatabaseStats();

        imageSwitch.setChecked(showImages);
        imageSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SHOW_IMAGES", isChecked);
            editor.apply();
        });

        useCloudSwitch.setChecked(useCloud);
        useCloudSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            inputLayout.setEnabled(isChecked);
            databaseStats.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("USE_CLOUD", isChecked);
            editor.apply();
        });

        databaseStats.setVisibility(useCloud ? View.VISIBLE : View.INVISIBLE);
        inputLayout.setEnabled(useCloud);
        inputLayout.setStartIconOnClickListener(v -> Toast.makeText(SettingsActivity.this, SettingsActivity.this.getResources().getString(R.string.database_instruction), Toast.LENGTH_LONG).show());
        editTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(DatabaseHandler.getInstance(SettingsActivity.this).getDatabaseId())) {
                    inputLayout.setEndIconVisible(true);
                    inputLayout.setBoxStrokeColor(getResources().getColor(R.color.dark_green));
                    return;
                }

                if (s.length() == DatabaseHandler.DATABASE_ID_LENGTH) {
                    DatabaseHandler.getInstance(SettingsActivity.this).changeDatabase(s.toString());
                    return;
                }
                if (s.length() < DatabaseHandler.DATABASE_ID_LENGTH) {
                    inputLayout.setError(null);
                    inputLayout.setEndIconVisible(false);
                    inputLayout.setBoxStrokeColor(getResources().getColor(R.color.teal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void updateDatabaseStats() {
        if (!DatabaseHandler.getInstance(this).isConnected()) {
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
                inputLayout.setBoxStrokeColor(getResources().getColor(R.color.dark_green));
                inputLayout.setEndIconVisible(true);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                break;
            case DATABASE_NOT_FOUND:
                inputLayout.setError(getResources().getString(R.string.database_not_found));
                break;
            case GAME_ADDED:
                updateDatabaseStats();
                break;
            case DATABASE_USER_REMOVED:
                updateDatabaseStats();
                break;
            case CREATED_TIMESTAMP_ADDED:
                created = databaseHandler.getCreated();
                createdTV.setText(created);
                break;
        }
    }
}
