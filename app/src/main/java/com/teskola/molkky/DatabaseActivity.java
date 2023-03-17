package com.teskola.molkky;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

public class DatabaseActivity extends AppCompatActivity implements DatabaseHandler.DatabaseListener {
    @Override
    protected void onStart() {
        super.onStart();
        DatabaseHandler.getInstance(DatabaseActivity.this).addListener(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        DatabaseHandler.getInstance(DatabaseActivity.this).removeListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseHandler.getInstance(DatabaseActivity.this).removeListener(this);

    }

    @Override
    public void onError(DatabaseHandler.Error error) {
        switch (error) {
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
            case GAME_ADDED:
                Toast.makeText(DatabaseActivity.this, getResources().getString(R.string.database_game_added), Toast.LENGTH_SHORT).show();
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
        }
    }
}
