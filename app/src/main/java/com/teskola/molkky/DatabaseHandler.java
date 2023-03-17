package com.teskola.molkky;

import static com.teskola.molkky.FirebaseManager.addTimestamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class DatabaseHandler {

    public static final int ID_LENGTH = 6;     // only even numbers allowed
    public static final int RETRY_CONNECTION =  10000;

    private final ArrayList<DatabaseListener> listeners = new ArrayList<>();
    private static DatabaseHandler instance;
    private FirebaseManager firebaseManager;
    private boolean useCloud;
    private String databaseId;
    private final SharedPreferences preferences;
    private final Handler handler = new Handler();
    private boolean retrySigningRunning = false;
    private final Runnable retrySignIn = new Runnable() {
        @Override
        public void run() {
            retrySigningRunning = true;
            signIn();
            handler.postDelayed(this, RETRY_CONNECTION);
        }
    };

    public String getDatabaseId() {
        return databaseId;
    }

    public interface DatabaseListener {
        void onError(Error error);
        void onDatabaseEvent(Event event);
    }

    public enum Event {
        DATABASE_FOUND,
        DATABASE_NOT_FOUND,
        DATABASE_CHANGED,
        DATABASE_CREATED,
        DATABASE_CONNECTED,
        DATABASE_DISCONNECTED,
        GAME_ADDED
    }

    public enum Error {
        NETWORK_ERROR,
        UNKNOWN_ERROR,
        ADD_GAME_FAILED
    }

    public String getShortId() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            signIn();
            return "";
        }
        return ((uid.substring(0, ID_LENGTH / 2) + uid.substring(uid.length() - (ID_LENGTH / 2))).toLowerCase());
    }

    public void addListener(DatabaseListener listener) {
        listeners.add(listener);
    }
    public void removeListener(DatabaseListener listener) {
        listeners.remove(listener);
    }

    public static DatabaseHandler getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHandler(context.getApplicationContext());
        return instance;
    }

    private DatabaseHandler(Context context) {
        preferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        useCloud = preferences.getBoolean("USE_CLOUD_DATABASE", false);
        databaseId = preferences.getString("DATABASE", generateDatabaseId());
        if (!useCloud)
            return;
        initializeFirebaseManager();
    }

    private void initializeFirebaseManager () {
        firebaseManager = new FirebaseManager();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            signIn();
        }
    }

    public void changeDatabase (String newDatabaseId) {

        firebaseManager.searchDatabase(newDatabaseId, response -> {
            if (response.equals("null")) {
                for (DatabaseListener listener : listeners)
                    listener.onDatabaseEvent(Event.DATABASE_NOT_FOUND);
                return;
            }
            for (DatabaseListener listener : listeners)
                listener.onDatabaseEvent(Event.DATABASE_FOUND);
            firebaseManager.removeUser(databaseId, FirebaseAuth.getInstance().getUid(),
                    unused1 -> firebaseManager.addUser(newDatabaseId, FirebaseAuth.getInstance().getUid(), unused11 -> {
                        for (DatabaseListener listener : listeners)
                            listener.onDatabaseEvent(Event.DATABASE_CHANGED);
                    }, e -> {
                        for (DatabaseListener listener : listeners)
                            listener.onError(Error.UNKNOWN_ERROR);
                    }), e -> {
                        for (DatabaseListener listener : listeners)
                            listener.onError(Error.UNKNOWN_ERROR);
                    });
        }, e -> {
            for (DatabaseListener listener : listeners)
                listener.onDatabaseEvent(Event.DATABASE_NOT_FOUND);
        });
    }

    public boolean getUseCloud() {
        return useCloud;
    }

    public void signIn() {

            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener
                    (authResult -> {
                        firebaseManager.initializeDatabase(getShortId(), response -> {
                            databaseId = getShortId();
                            for (DatabaseListener listener : listeners)
                                listener.onDatabaseEvent(Event.DATABASE_CONNECTED);
                            firebaseManager.addUser(getShortId(), FirebaseAuth.getInstance().getUid(), unused -> {
                                for (DatabaseListener listener : listeners)
                                    listener.onDatabaseEvent(Event.DATABASE_CREATED);
                            }, error -> {
                                for (DatabaseListener listener : listeners)
                                    listener.onError(Error.UNKNOWN_ERROR);
                            });
                        }, error -> {
                            for (DatabaseListener listener : listeners)
                                listener.onError(Error.NETWORK_ERROR);
                        });
            });
    }

    public void setUseCloud(boolean useCloud) {
        this.useCloud = useCloud;
        if (useCloud)
            initializeFirebaseManager();
        else {
            firebaseManager = null;
            handler.removeCallbacks(retrySignIn);
            retrySigningRunning = false;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("USE_CLOUD_DATABASE", useCloud);
        editor.apply();
    }

    /*
     *
     * Returns 6 character, lowercase, version of userId used as database id.
     *
     * */

    private String generateDatabaseId() {
        if (!useCloud || FirebaseAuth.getInstance().getUid() == null)
            return "";
        String uid = FirebaseAuth.getInstance().getUid();
        return ((uid.substring(0, ID_LENGTH / 2) + uid.substring(uid.length() - (ID_LENGTH / 2))).toLowerCase());
    }

    public void saveGame(Context context, Game game) {
        LocalDatabaseManager.getInstance(context).saveGameToDatabase(game);
        if (useCloud)

            firebaseManager.addGameToDatabase(game, FirebaseAuth.getInstance().getUid(), new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    game.setId(s);
                    for (DatabaseListener listener : listeners)
                        listener.onDatabaseEvent(Event.GAME_ADDED);
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    for (DatabaseListener listener : listeners)
                        listener.onError(Error.ADD_GAME_FAILED);
                }
            });
    }

    private JSONObject gameToJson(Game game) {
        String json = new Gson().toJson(game);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            JSONArray players = jsonObject.getJSONArray("players");
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                player.remove("undoStack");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
