package com.teskola.molkky;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.util.ArrayList;

public class DatabaseHandler implements FirebaseAuth.AuthStateListener, FirebaseAuth.IdTokenListener {

    public static final int ID_LENGTH = 6;     // only even numbers allowed

    private final ArrayList<DatabaseListener> listeners = new ArrayList<>();
    private static DatabaseHandler instance;
    private FirebaseManager firebaseManager;
    private boolean useCloud;
    private ArrayList<String> databases; // TODO
    private Database database;
    private Context context;

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        firebaseAuth.removeAuthStateListener(this);
        onIdTokenChanged(firebaseAuth);
        for (DatabaseListener listener : listeners) listener.onDatabaseEvent(Event.DATABASE_CONNECTED);
    }

    @Override
    public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null)
            firebaseAuth.getCurrentUser().getIdToken(false).addOnSuccessListener(getTokenResult -> {
                if (firebaseManager != null)
                    firebaseManager.setToken(getTokenResult.getToken());
            });
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
        this.context = context;
        useCloud = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE).getBoolean("USE_CLOUD_DATABASE", false);
        database = new Database(context.getSharedPreferences("DATABASES", Context.MODE_PRIVATE).getString("CURRENT", generateDatabaseId()));
        if (!useCloud)
            return;
        firebaseManager = new FirebaseManager(context);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            signIn();
            return;
        }
        fetchDatabase(database.getId());
    }

    public void fetchDatabase (String id) {
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false).addOnSuccessListener(getTokenResult -> {
            firebaseManager.setToken(getTokenResult.getToken());
            firebaseManager.fetchDatabase(id)
                    .addOnSuccessListener(response -> {
                                if (!response.equals("null")) {
                                    firebaseManager.disconnect(database.getId(), FirebaseAuth.getInstance().getUid());
                                    database = new Gson().fromJson(response, Database.class);
                                    firebaseManager.addUser(id, FirebaseAuth.getInstance().getUid());
                                    for (DatabaseListener listener : listeners) listener.onDatabaseEvent(Event.DATABASE_CHANGED);
                                    context.getSharedPreferences("DATABASES", Context.MODE_PRIVATE).edit().putString("CURRENT", id).apply();

                                }
                                else {
                                    for (DatabaseListener listener : listeners) listener.onDatabaseEvent(Event.DATABASE_NOT_FOUND);
                                }
                            }
                    ).addOnFailureListener(this::errorResponse);
        });
    }

    private void errorResponse (int errorCode) {
        if (errorCode == FirebaseManager.NETWORK_ERROR)
            for (DatabaseListener listener : listeners) listener.onError(Error.NETWORK_ERROR);
        else
            for (DatabaseListener listener : listeners) listener.onError(Error.UNKNOWN_ERROR);
    }

    public boolean getUseCloud() {
        return useCloud;
    }

    public void signIn() {

            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener
                    (authResult -> {
                        FirebaseAuth.getInstance().getCurrentUser().getIdToken(false)
                                .addOnSuccessListener(getTokenResult -> {
                                    firebaseManager.setToken(getTokenResult.getToken());
                                    database.setId(generateDatabaseId());
                                    firebaseManager.initializeDatabase(database.getId())
                                            .addOnSuccessListener(response -> {
                                                for (DatabaseListener listener : listeners)
                                                    listener.onDatabaseEvent(Event.DATABASE_CONNECTED);

                                            }).addUser(generateDatabaseId(), FirebaseAuth.getInstance().getUid());
                                });

                    }).addOnFailureListener(e -> {
                for (DatabaseListener listener : listeners)
                    listener.onError(Error.NETWORK_ERROR);
            });


    }

    public void setUseCloud(boolean useCloud) {
        this.useCloud = useCloud;
        if (useCloud)
            firebaseManager = new FirebaseManager(context.getApplicationContext());
        else {
            firebaseManager.close();
            firebaseManager = null;
        }
        SharedPreferences.Editor editor = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE).edit();
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

    public Database getDatabase() {
        return database;
    }

    public void saveGame(Context context, Game game) {
        LocalDatabaseManager.getInstance(context).saveGameToDatabase(game);
        if (useCloud)
            firebaseManager.addGameToFireBase(database.getId(), game, FirebaseAuth.getInstance().getUid());
    }

}
