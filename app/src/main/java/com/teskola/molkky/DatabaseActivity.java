package com.teskola.molkky;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

public abstract class DatabaseActivity extends AppCompatActivity implements DatabaseHandler.DatabaseListener {
    @Override
    protected void onResume() {
        super.onResume();
        DatabaseHandler.getInstance(DatabaseActivity.this).addListener(this);
        if (DatabaseHandler.getInstance(DatabaseActivity.this).isNotConnected())
            DatabaseHandler.getInstance(DatabaseActivity.this).signIn();
        else
            DatabaseHandler.getInstance(DatabaseActivity.this).start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        DatabaseHandler.getInstance(DatabaseActivity.this).removeListener(this);
        DatabaseHandler.getInstance(DatabaseActivity.this).stop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseHandler.getInstance(DatabaseActivity.this).removeListener(this);

    }

    @Override
    public void onError(DatabaseHandler.Error error) {
        switch (error) {
            case UNKNOWN_ERROR:
                break;
            case SPECTATOR_MODE_UNAVAILABLE:
                Toast.makeText(DatabaseActivity.this, R.string.spectator_mode_error, Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                break;
            case NETWORK_ERROR:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_connection_failed), Toast.LENGTH_SHORT).show();
                break;
            case ADD_GAME_FAILED:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_game_added_failed), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onDatabaseEvent(DatabaseHandler.Event event) {
        switch (event) {
            case DATABASE_FOUND:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_found), Toast.LENGTH_SHORT).show();
                break;
            case DATABASE_NEWUSER:
                break;
            case DATABASE_USER_REMOVED:
                break;
            case GAME_ADDED:
                // Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_game_added), Toast.LENGTH_SHORT).show();
                break;
            case DATABASE_CHANGED:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_changed), Toast.LENGTH_SHORT).show();
                break;
            case DATABASE_CREATED:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_created), Toast.LENGTH_SHORT).show();
                break;
            case DATABASE_DISCONNECTED:
                break;
            case DATABASE_CONNECTED:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_connected), Toast.LENGTH_SHORT).show();
                break;
            case DATABASE_NOT_FOUND:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_not_found), Toast.LENGTH_SHORT).show();
                break;
            case SPECTATOR_MODE_AVAILABLE:
                Toast.makeText(DatabaseActivity.this, R.string.spectator_mode_start, Toast.LENGTH_SHORT).show();
                break;
            case CREATED_TIMESTAMP_ADDED:
                break;
            case PLAYER_ADDED_FROM_DATABASE:
                break;
        }
    }
}
