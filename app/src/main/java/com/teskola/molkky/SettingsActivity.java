package com.teskola.molkky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends CommonOptions {
    private SwitchCompat imageSwitch;
    private SwitchCompat cloudSwitch;
    private Button confirmBtn;
    private TextView idTV, editTV, instructionsTV;

    private boolean showImages;
    private boolean useCloud;
    private SharedPreferences preferences;
    private FBHandler fbHandler;
    private String newDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        confirmBtn = findViewById(R.id.confirmButton);
        imageSwitch = findViewById(R.id.imageSwitch);
        cloudSwitch = findViewById(R.id.cloudSwitch);
        idTV = findViewById(R.id.idTV);
        editTV = findViewById(R.id.editDBID);
        instructionsTV = findViewById(R.id.instructionsTV);

        fbHandler = new FBHandler(this);
        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        String database = preferences.getString("DATABASE", fbHandler.getShortId());
        editTV.setText(database);
        showImages = preferences.getBoolean("SHOW_IMAGES", false);
        useCloud = preferences.getBoolean("USE_CLOUD_DATABASE", false);
        imageSwitch.setChecked(showImages);
        cloudSwitch.setChecked(useCloud);
        setDBOptionsColors();

        imageSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            showImages = isChecked;
        });

        cloudSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            useCloud = isChecked;
            setDBOptionsColors();
        });

        editTV.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (textView.getText().length() != 6) {
                    textView.setText(database);
                    Toast.makeText(this, getString(R.string.too_short_id), Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        });

        editTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 5) {
                    validateDatabaseInput(s.toString());
                }
                else {
                    newDatabase = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });





        confirmBtn.setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SHOW_IMAGES", showImages);
            editor.putBoolean("USE_CLOUD_DATABASE", useCloud);

            String db = editTV.getText().toString();
            if (db.length() == 6 && newDatabase != null && useCloud) {
                editor.putString("DATABASE", db);
            }
            editor.apply();
            finish();
        });

    }

    public void setDBOptionsColors () {
        if (useCloud) {
            idTV.setTextColor(getResources().getColor(R.color.black));
            editTV.setTextColor(getResources().getColor(R.color.black));
            editTV.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            instructionsTV.setTextColor(getResources().getColor(R.color.black));
        } else {
            idTV.setTextColor(getResources().getColor(R.color.light_gray));
            editTV.setTextColor(getResources().getColor(R.color.light_gray));
            editTV.setInputType(InputType.TYPE_NULL);
            instructionsTV.setTextColor(getResources().getColor(R.color.light_gray));
        }
    }
    public void validateDatabaseInput(String id) {
        fbHandler.setOnResponseListener(new FBHandler.onResponseListener() {
            @Override
            public void onResponseReceived(String response) {
                if (response.equals("null")) {
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.database_not_found), Toast.LENGTH_SHORT).show();
                    newDatabase = null;
                    String database = preferences.getString("DATABASE", fbHandler.getShortId());
                    editTV.setText(database);
                }
                else {
                    newDatabase = response;

                }
            }
        });
        fbHandler.getGamesJson(id);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.settings).setVisible(false);
        return true;
    }

}