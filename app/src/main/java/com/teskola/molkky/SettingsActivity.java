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

public class SettingsActivity extends DatabaseActivity {
    private SwitchCompat imageSwitch;
    private SwitchCompat cloudSwitch;
    private TextView editTV;
    private ImageButton infoButton;
    private ViewGroup databaseStats;

    private boolean showImages;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        imageSwitch = findViewById(R.id.imageSwitch);
        cloudSwitch = findViewById(R.id.cloudSwitch);
        editTV = findViewById(R.id.editDBID);
        infoButton = findViewById(R.id.infoButton);
        databaseStats = findViewById(R.id.databaseStatsView);

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        showImages = preferences.getBoolean("SHOW_IMAGES", false);

        editTV.setText(DatabaseHandler.getInstance(this).getDatabaseId());
        editTV.setImeActionLabel(getResources().getString(R.string.confirm), EditorInfo.IME_ACTION_DONE);
        imageSwitch.setChecked(showImages);
        cloudSwitch.setChecked(DatabaseHandler.getInstance(this).getUseCloud());
        setDBOptionsColors();
        imageSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            showImages = isChecked;
        });
        cloudSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            DatabaseHandler.getInstance(this).setUseCloud(isChecked);
            setDBOptionsColors();
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
            if (cloudSwitch.isChecked()) {
                Toast.makeText(this, getResources().getString(R.string.database_instruction), Toast.LENGTH_LONG).show();
            }
        });

    }

    public void checkTextLength() {
        String input = editTV.getText().toString();
        if (input.length() < DatabaseHandler.ID_LENGTH) {
            Toast.makeText(this, getString(R.string.too_short_id), Toast.LENGTH_SHORT).show();
        }
        editTV.setText(DatabaseHandler.getInstance(this).getDatabaseId());
    }

    // https://stackoverflow.com/questions/20121938/how-to-set-tint-for-an-image-view-programmatically-in-android

    public void setDBOptionsColors() {
        if (cloudSwitch.isChecked()) {
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
    }


}
