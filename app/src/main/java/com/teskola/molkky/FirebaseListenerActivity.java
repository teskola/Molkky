package com.teskola.molkky;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

public abstract class FirebaseListenerActivity extends AppCompatActivity implements FirebaseListener {
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        if (preferences.getBoolean("USE_CLOUD_DATABASE", true)) {
            FirebaseManager.getInstance(FirebaseListenerActivity.this).addListener(this);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseManager.getInstance(FirebaseListenerActivity.this).removeListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseManager.getInstance(FirebaseListenerActivity.this).removeListener(this);

    }

    @Override
    public void onSignInCompleted() {
        Toast.makeText(FirebaseListenerActivity.this, getResources().getString(R.string.database_connected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSignInFailed() {
        Toast.makeText(FirebaseListenerActivity.this,getResources().getString(R.string.database_connection_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponseReceived(FirebaseManager.Response response, String data) {
        switch (response) {
            case DATABASE_FOUND:
                Toast.makeText(FirebaseListenerActivity.this, getResources().getString(R.string.database_found), Toast.LENGTH_SHORT).show();
                break;
            case DATABASE_NOT_FOUND:
                break;
            case DATABASE_CHANGED:
                break;
            case DATABASE_CREATED:
                break;
            case DATABASE_CONNECTED:
                break;
            case DATABASE_DISCONNECTED:
                Toast.makeText(FirebaseListenerActivity.this, getResources().getString(R.string.database_changed), Toast.LENGTH_SHORT).show();
                break;
            case GAME_ADDED:
                Toast.makeText(FirebaseListenerActivity.this, getResources().getString(R.string.database_game_added), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onErrorReceived(FirebaseManager.Error error) {
        switch (error) {
            default:
                Toast.makeText(FirebaseListenerActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                break;
            case NETWORK_ERROR:
                Toast.makeText(FirebaseListenerActivity.this, getResources().getString(R.string.database_connection_failed), Toast.LENGTH_SHORT).show();
                break;
            case ADD_GAME_FAILED:
                Toast.makeText(FirebaseListenerActivity.this, getResources().getString(R.string.database_game_added_failed), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
