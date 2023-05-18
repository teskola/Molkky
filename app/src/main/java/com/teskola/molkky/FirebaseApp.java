package com.teskola.molkky;

import android.app.Application;
import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (this.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE).getBoolean("SHOW_IMAGES", false))
            ImageHandler.getInstance(this);
    }
}
