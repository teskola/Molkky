package com.teskola.molkky;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public abstract class FirebaseActivity extends AppCompatActivity {


    @Override
    protected void onResume() {
        super.onResume();
        FirebaseManager.getInstance(this).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseManager.getInstance(this).stop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}