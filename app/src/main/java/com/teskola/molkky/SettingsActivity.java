package com.teskola.molkky;

import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends FirebaseActivity implements MetaHandler.DatabaseListener {
    private MetaHandler databaseHandler;

    private SwitchCompat imageSwitch;
    private TextView gamesTV, playersTV, tossesTV, createdTV, updatedTV;
    private ViewGroup databaseStats;
    private EditText editTV;
    private TextInputLayout inputLayout;
    private boolean showImages;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHandler = new MetaHandler(this, this);
        setContentView(R.layout.activity_settings);

        imageSwitch = findViewById(R.id.imageSwitch);
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
        String dbid = preferences.getString("DATABASE", null);
        editTV.setText(dbid);
        editTV.setSelection(editTV.getText().length());         // cursor to the end of text field

        imageSwitch.setChecked(showImages);
        imageSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SHOW_IMAGES", isChecked);
            editor.apply();
        });


        inputLayout.setStartIconOnClickListener(v -> Toast.makeText(SettingsActivity.this, SettingsActivity.this.getResources().getString(R.string.database_instruction), Toast.LENGTH_LONG).show());
        editTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String dbid = preferences.getString("DATABASE", null);
                if (s.toString().equals(dbid)) {
                    inputLayout.setEndIconVisible(true);
                    inputLayout.setBoxStrokeColor(getResources().getColor(R.color.dark_green));
                    return;
                }

                if (s.length() == FirebaseManager.DATABASE_ID_LENGTH) {
                    databaseHandler.changeDatabase(s.toString());
                    return;
                }
                if (s.length() < FirebaseManager.DATABASE_ID_LENGTH) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHandler.close();
    }

    @Override
    public void onError(MetaHandler.Error error) {
        if (error == MetaHandler.Error.UNKNOWN_ERROR) {
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
        }
        else if (error == MetaHandler.Error.NETWORK_ERROR)
            Toast.makeText(this, R.string.database_connection_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDatabaseEvent(MetaHandler.Event event) {
        if (event == MetaHandler.Event.DATABASE_FOUND) {
            inputLayout.setBoxStrokeColor(getResources().getColor(R.color.dark_green));
            inputLayout.setEndIconVisible(true);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } else if (event == MetaHandler.Event.DATABASE_NOT_FOUND) {
            inputLayout.setError(getResources().getString(R.string.database_not_found));
        }
    }

    @Override
    public void onGamesReceived(int count) {
        databaseStats.setVisibility(View.VISIBLE);
        gamesTV.setText(String.valueOf(count));
    }

    @Override
    public void onPlayersReceived(int count) {
        databaseStats.setVisibility(View.VISIBLE);
        playersTV.setText(String.valueOf(count));
    }

    @Override
    public void onTossesReceived(int count) {
        databaseStats.setVisibility(View.VISIBLE);
        tossesTV.setText(String.valueOf(count));
    }

    @Override
    public void onUpdatedReceived(String date) {
        databaseStats.setVisibility(View.VISIBLE);
        updatedTV.setText(date);
    }

    @Override
    public void onCreatedReceived(String date) {
        databaseStats.setVisibility(View.VISIBLE);
        createdTV.setText(date);
        if (updatedTV.getText().equals(""))
            updatedTV.setText(date);
    }
}
