package com.teskola.molkky;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    private FirebaseManager.UserStatusListener userStatusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        if (preferences.getBoolean("USE_CLOUD_DATABASE", true) && FirebaseManager.getInstance(this).getUser() == null ) {
            userStatusListener = new FirebaseManager.UserStatusListener() {
                @Override
                public void onSuccessfulSignIn(FirebaseAuth firebaseAuth) {
                    Toast.makeText(BaseActivity.this, getResources().getString(R.string.database_connected), Toast.LENGTH_SHORT).show();
                    FirebaseManager.getInstance(BaseActivity.this).removeUserStatusListener(this);
                }

                @Override
                public void onFailedSignIn(Exception e) {
                    Toast.makeText(BaseActivity.this,getResources().getString(R.string.database_connection_failed), Toast.LENGTH_SHORT).show();
                }
            };
            FirebaseManager.getInstance(BaseActivity.this).addUserStatusListener(userStatusListener);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (userStatusListener != null) {
            FirebaseManager.getInstance(BaseActivity.this).removeUserStatusListener(userStatusListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userStatusListener != null) {
            FirebaseManager.getInstance(BaseActivity.this).removeUserStatusListener(userStatusListener);
        }
    }

}