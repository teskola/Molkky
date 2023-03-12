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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends FirebaseListenerActivity {
    private SwitchCompat imageSwitch;
    private SwitchCompat cloudSwitch;
    private Button confirmBtn;
    private TextView editTV;
    private ImageButton infoButton;
    private ViewGroup databaseStats;

    private boolean showImages;
    private boolean useCloud;
    private SharedPreferences preferences;
    private String newDatabaseId;
    private String originalDatabaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        confirmBtn = findViewById(R.id.confirmButton);
        imageSwitch = findViewById(R.id.imageSwitch);
        cloudSwitch = findViewById(R.id.cloudSwitch);
        editTV = findViewById(R.id.editDBID);
        infoButton = findViewById(R.id.infoButton);
        databaseStats = findViewById(R.id.databaseStatsView);

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        showImages = preferences.getBoolean("SHOW_IMAGES", false);
        useCloud = preferences.getBoolean("USE_CLOUD_DATABASE", true);
        if (useCloud) {
            originalDatabaseId = preferences.getString("DATABASE", FirebaseManager.getInstance(this).getShortId());
        } else {
            originalDatabaseId = preferences.getString("DATABASE", "");
        }
        editTV.setText(originalDatabaseId);
        editTV.setImeActionLabel(getResources().getString(R.string.confirm), EditorInfo.IME_ACTION_DONE);
        imageSwitch.setChecked(showImages);
        cloudSwitch.setChecked(useCloud);
        setDBOptionsColors();
        imageSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            showImages = isChecked;
        });
        cloudSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            originalDatabaseId = preferences.getString("DATABASE", FirebaseManager.getInstance(this).getShortId());
            useCloud = isChecked;
            setDBOptionsColors();
            if (isChecked) {
                editTV.setText(originalDatabaseId);
                FirebaseManager.getInstance(this).addListener(this);
            }
            else
                FirebaseManager.getInstance(this).removeListener(this);
        });

        editTV.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
               refreshEditText();
            }
            return false;
        });

        editTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == FirebaseManager.ID_LENGTH && !s.toString().equals(originalDatabaseId) && (newDatabaseId == null || !newDatabaseId.equals(s.toString())))
                    Log.d("Fetch", "fetch database");
                    // FirebaseManager.getInstance(SettingsActivity.this).fetchDatabase(s.toString(), false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        infoButton.setOnClickListener(view -> {
            if (useCloud) {
                Toast.makeText(this, getResources().getString(R.string.database_instruction), Toast.LENGTH_LONG).show();
            }
        });


        confirmBtn.setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SHOW_IMAGES", showImages);
            editor.putBoolean("USE_CLOUD_DATABASE", useCloud);
            if (useCloud && newDatabaseId != null && !newDatabaseId.equals(originalDatabaseId)) {
                FirebaseManager.getInstance(this).disconnect(originalDatabaseId);
                FirebaseManager.getInstance(this).addUser(newDatabaseId);
                editor.putString("DATABASE", newDatabaseId);
            }
            editor.apply();
            finish();

        });

    }

    public void refreshEditText() {
        String input = editTV.getText().toString();
        if (!originalDatabaseId.equals("") && input.length() < FirebaseManager.ID_LENGTH) {
            Toast.makeText(this, getString(R.string.too_short_id), Toast.LENGTH_SHORT).show();
        }
        if (newDatabaseId != null) {
            editTV.setText(newDatabaseId);
        } else {
            editTV.setText(originalDatabaseId);
        }
    }

    // https://stackoverflow.com/questions/20121938/how-to-set-tint-for-an-image-view-programmatically-in-android

    public void setDBOptionsColors() {
        if (useCloud) {
            editTV.setTextColor(getResources().getColor(R.color.black));
            editTV.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            infoButton.setColorFilter(ContextCompat.getColor(this, R.color.teal), PorterDuff.Mode.SRC_IN);

        } else {
            editTV.setTextColor(getResources().getColor(R.color.light_gray));
            editTV.setInputType(InputType.TYPE_NULL);
            infoButton.setColorFilter(ContextCompat.getColor(this, R.color.light_gray), PorterDuff.Mode.SRC_IN);
            databaseStats.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null && ev.getAction() == MotionEvent.ACTION_UP) {
            refreshEditText();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onSignInCompleted() {
        super.onSignInCompleted();
        originalDatabaseId = preferences.getString("DATABASE", FirebaseManager.getInstance(SettingsActivity.this).getShortId());
        editTV.setText(originalDatabaseId);
    }

    @Override
    public void onResponseReceived(FirebaseManager.Response response, String databaseId) {
        super.onResponseReceived(response, databaseId);
        if (response == FirebaseManager.Response.DATABASE_FOUND) {
            newDatabaseId = databaseId;
        }
        if (response == FirebaseManager.Response.DATABASE_NOT_FOUND)
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.database_not_found), Toast.LENGTH_SHORT).show();

    }
}
