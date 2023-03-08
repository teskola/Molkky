package com.teskola.molkky;

import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends CommonOptions {
    private SwitchCompat imageSwitch;
    private SwitchCompat cloudSwitch;
    private Button confirmBtn;
    private TextView idTV, editTV, instructionsTV;

    private boolean showImages;
    private boolean useCloud;
    private SharedPreferences preferences;
    private String current;

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

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        current = preferences.getString("DATABASE", FBHandler.getInstance(getApplicationContext()).getShortId());
        editTV.setText(current);
        editTV.setImeActionLabel(getResources().getString(R.string.confirm), EditorInfo.IME_ACTION_DONE);
        showImages = preferences.getBoolean("SHOW_IMAGES", false);
        useCloud = preferences.getBoolean("USE_CLOUD_DATABASE", true);
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
                String input = textView.getText().toString();
                if (input.length() < FBHandler.ID_LENGTH) {
                    discardChanges();
                } else if (!input.equals(current)) {
                    validateDatabaseInput(input);
                }
            }
            return false;
        });

        confirmBtn.setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SHOW_IMAGES", showImages);
            editor.putBoolean("USE_CLOUD_DATABASE", useCloud);

            String editText = editTV.getText().toString();
            String database = preferences.getString("DATABASE", FBHandler.getInstance(getApplicationContext()).getShortId());
            if (editText.length() == FBHandler.ID_LENGTH && !editText.equals(database) && useCloud) {
                FBHandler.getInstance(getApplicationContext()).disconnect(database);
                FBHandler.getInstance(getApplicationContext()).addUser(editText);
                editor.putString("DATABASE", editText);
            }
            editor.apply();
            finish();
        });

    }

    public void setDBOptionsColors() {
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

    public void validateDatabaseInput(String input) {
        FBHandler fbHandler = FBHandler.getInstance(getApplicationContext());
        fbHandler.setOnResponseListener(new FBHandler.onResponseListener() {
            @Override
            public void onResponseReceived(JSONObject response) {

                try {
                    String created = response.getString("created");
                    if (created.equals("null")) {
                        Toast.makeText(SettingsActivity.this, getResources().getString(R.string.database_not_found), Toast.LENGTH_SHORT).show();
                        editTV.setText(current);
                    }
                    else {
                        current = input;
                        Toast.makeText(SettingsActivity.this, getResources().getString(R.string.database_changed), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onErrorReceived(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    fbHandler.refreshToken();
                } else {
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.database_read_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSignIn() {
                fbHandler.refreshToken();
            }

            @Override
            public void onTokenRefreshed() {
                current = preferences.getString("DATABASE", fbHandler.getShortId());
                fbHandler.testDatabase(current, true);
                fbHandler.testDatabase(input, false);
            }

            @Override
            public void onSignInFailed() {
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.database_read_error), Toast.LENGTH_SHORT).show();
            }
        });
        if (fbHandler.getUser() != null) {
            fbHandler.refreshToken();
        } else {
            fbHandler.signIn();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null && ev.getAction() == MotionEvent.ACTION_UP) {
            String input = editTV.getText().toString();
            if (input.length() < FBHandler.ID_LENGTH) {
                discardChanges();
            } else if (!input.equals(current)) {
                validateDatabaseInput(input);
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void discardChanges() {

        editTV.setText(current);
        Toast.makeText(this, getString(R.string.too_short_id), Toast.LENGTH_SHORT).show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.settings).setVisible(false);
        return true;
    }

}