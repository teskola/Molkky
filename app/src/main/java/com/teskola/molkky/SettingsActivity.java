package com.teskola.molkky;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
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

public class SettingsActivity extends DatabaseActivity {
    private DatabaseHandler databaseHandler = DatabaseHandler.getInstance(this);

    private SwitchCompat imageSwitch;
    private SwitchCompat allowSpectatorsSwitch;
    private TextView editTV, gamesTV, playersTV, tossesTV, createdTV, updatedTV;
    private ImageButton infoButton;
    private ViewGroup databaseStats;


    private boolean showImages;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imageSwitch = findViewById(R.id.imageSwitch);
        allowSpectatorsSwitch = findViewById(R.id.allowSpectatorsSwitch);
        editTV = findViewById(R.id.editDBID);
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
            showImages = isChecked;
        });

        editTV.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
               checkTextLength();
            }
            return false;
        });

        editTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == DatabaseHandler.ID_LENGTH && !s.toString().equals(DatabaseHandler.getInstance(SettingsActivity.this).getDatabaseId()))
                    DatabaseHandler.getInstance(SettingsActivity.this).changeDatabase(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        infoButton.setOnClickListener(view -> {
          Toast.makeText(this, getResources().getString(R.string.database_instruction), Toast.LENGTH_LONG).show();
        });

    }

    public void checkTextLength() {
        String input = editTV.getText().toString();
        if (input.length() < DatabaseHandler.ID_LENGTH) {
            Toast.makeText(this, getString(R.string.too_short_id), Toast.LENGTH_SHORT).show();
        }
        editTV.setText(DatabaseHandler.getInstance(this).getDatabaseId());
    }

    public void updateDatabaseStats() {
        int games = databaseHandler.getGamesCount();
        int players = databaseHandler.getPlayersCount();
        int tosses = databaseHandler.getTossesCount();
        String created = databaseHandler.getCreated();
        String updated = databaseHandler.getUpdated();
        gamesTV.setText(String.valueOf(games));
        playersTV.setText(String.valueOf(players));
        tossesTV.setText(String.valueOf(tosses));
        updatedTV.setText(updated);
        if (databaseHandler.getCreated() != null)
            createdTV.setText(created);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!DatabaseHandler.getInstance(this).getDatabaseId().equals("") && getCurrentFocus() != null && ev.getAction() == MotionEvent.ACTION_UP) {
            checkTextLength();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDatabaseEvent(DatabaseHandler.Event event) {
        super.onDatabaseEvent(event);
        if (event == DatabaseHandler.Event.DATABASE_NOT_FOUND || event == DatabaseHandler.Event.DATABASE_CREATED)
            editTV.setText(DatabaseHandler.getInstance(this).getDatabaseId());
        if (event == DatabaseHandler.Event.DATABASE_CHANGED)
            updateDatabaseStats();
        if (event == DatabaseHandler.Event.CREATED_TIMESTAMP_ADDED) {
            String created = databaseHandler.getCreated();
            createdTV.setText(created);
        }
    }
}
