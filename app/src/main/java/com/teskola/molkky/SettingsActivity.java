package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends BaseActivity {
    private SwitchCompat imageSwitch;
    private SwitchCompat cloudSwitch;
    private Button confirmBtn;
    private TextView editTV, instructionsTV;

    private boolean showImages;
    private boolean useCloud;
    private SharedPreferences preferences;
    private String current;
    private String input;
    private FirebaseAuth.AuthStateListener authStateListener;
    private final FirebaseManagerListener validationListener = new FirebaseManagerListener() {
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
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSignInFailed(Exception e) {
            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.database_read_error), Toast.LENGTH_SHORT).show();

        }
    };;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        confirmBtn = findViewById(R.id.confirmButton);
        imageSwitch = findViewById(R.id.imageSwitch);
        cloudSwitch = findViewById(R.id.cloudSwitch);
        editTV = findViewById(R.id.editDBID);
        instructionsTV = findViewById(R.id.instructionsTV);

        preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        current = preferences.getString("DATABASE", FirebaseManager.getInstance(this).getShortId());
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
            if (isChecked)
                FirebaseManager.getInstance(SettingsActivity.this).addListener(validationListener);
            else
                FirebaseManager.getInstance(SettingsActivity.this).removeListener(validationListener);

        });

        editTV.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                input = textView.getText().toString();
                if (input.length() < FirebaseManager.ID_LENGTH) {
                    discardChanges();
                } else if (!input.equals(current)) {
                    FirebaseManager.getInstance(SettingsActivity.this).testDatabase(input, false);
                }
            }
            return false;
        });

        confirmBtn.setOnClickListener(view -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("SHOW_IMAGES", showImages);
            editor.putBoolean("USE_CLOUD_DATABASE", useCloud);

            String editText = editTV.getText().toString();
            String database = preferences.getString("DATABASE", FirebaseManager.getInstance(this).getShortId());
            if (editText.length() == FirebaseManager.ID_LENGTH && !editText.equals(database) && useCloud) {
                FirebaseManager.getInstance(this).disconnect(database);
                FirebaseManager.getInstance(this).addUser(editText);
                editor.putString("DATABASE", editText);
            }
            editor.apply();
            finish();
        });

    }

    public void setDBOptionsColors() {
        if (useCloud) {
            editTV.setTextColor(getResources().getColor(R.color.black));
            editTV.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            instructionsTV.setTextColor(getResources().getColor(R.color.black));
        } else {
            editTV.setTextColor(getResources().getColor(R.color.light_gray));
            editTV.setInputType(InputType.TYPE_NULL);
            instructionsTV.setTextColor(getResources().getColor(R.color.light_gray));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null && ev.getAction() == MotionEvent.ACTION_UP) {
            String input = editTV.getText().toString();
            if (input.length() < FirebaseManager.ID_LENGTH) {
                discardChanges();
            } else if (!input.equals(current)) {
                FirebaseManager.getInstance(SettingsActivity.this).testDatabase(input, false);
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

    protected void onStart() {
        super.onStart();
        if (preferences.getBoolean("USE_CLOUD_DATABASE", true) && FirebaseManager.getInstance(this).getUser() == null ) {
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() != null) {
                        Toast.makeText(SettingsActivity.this, getResources().getString(R.string.database_connected), Toast.LENGTH_SHORT).show();
                        current = preferences.getString("DATABASE", FirebaseManager.getInstance(SettingsActivity.this).getShortId());
                        FirebaseAuth.getInstance().removeAuthStateListener(this);
                    }
                }
            };
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
        FirebaseManager.getInstance(this).removeListener(validationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
        FirebaseManager.getInstance(this).removeListener(validationListener);
    }
}