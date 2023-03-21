package com.teskola.molkky;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DatabaseHandler implements FirebaseManager.DatabaseListener {

    public static final int ID_LENGTH = 6;     // only even numbers allowed
    public static final int RETRY_CONNECTION = 10000;
    private final Database database;
    private final ArrayList<DatabaseListener> listeners = new ArrayList<>();
    private static DatabaseHandler instance;
    private FirebaseManager firebaseManager;
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

    @Override
    public void onNewUser() {
        for (DatabaseListener listener : listeners)
            listener.onDatabaseEvent(Event.DATABASE_NEWUSER);
    }

    @Override
    public void onUserRemoved() {

    }

    @Override
    public void onGameAdded() {
        for (DatabaseListener listener : listeners)
            listener.onDatabaseEvent(Event.GAME_ADDED);
    }

    @Override
    public void onGameRemoved() {

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
        DATABASE_NEWUSER,
        DATABASE_USER_REMOVED,
        GAME_ADDED,
        CREATED_TIMESTAMP_ADDED,
    }

    public enum Error {
        NETWORK_ERROR,
        UNKNOWN_ERROR,
        ADD_GAME_FAILED
    }

    /*
     *
     * Returns 6 character, lowercase, version of userId used as database id.
     *
     * */

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
        database = new Database();
        preferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        databaseId = preferences.getString("DATABASE", getShortId());
        connectToFirebase();
    }

    public void connectToFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            signIn();
            return;
        }
        if (firebaseManager == null)
            firebaseManager = new FirebaseManager(databaseId, database, this);
    }

    public void disconnectFromFirebase() {
        firebaseManager = null;
    }

    public void changeDatabase(String newDatabaseId) {

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
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("DATABASE", newDatabaseId);
                        databaseId = newDatabaseId;
                        editor.apply();
                        database.setCreated(Long.parseLong(response));
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

    public void signIn() {

        FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener
                (authResult -> {
                    firebaseManager = new FirebaseManager(database, this);
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

    public void saveGame(Context context, Game game) {
        // LocalDatabaseManager.getInstance(context).saveGameToDatabase(game);


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

    public void addPlayer(PlayerInfo player) {
        String playerId = database.getPlayerId(player.getName());
        if (playerId != null) {
            player.setId(playerId);
            // TODO ilmoitus käyttäjälle, pelaaja lisättiin tietokannasta
        } else {
            player.setId(UUID.randomUUID().toString().substring(0, 8));
        }
    }

    public ArrayList<GameInfo> getGames() {
        List<Game> games = database.getGames();
        ArrayList<GameInfo> result = new ArrayList<>();
        for (Game game : games) {
            String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(game.getTimestamp());
            GameInfo gameInfo = new GameInfo(game.getId(), timestamp, game.getPlayers().get(0).getName());
            result.add(gameInfo);
        }
        return result;
    }

    public ArrayList<GameInfo> getGames(String playerId) {
        List<Game> games = database.getGames();
        ArrayList<GameInfo> result = new ArrayList<>();
        for (Game game : games) {
            for (Player player : game.getPlayers()) {
                if (player.getId().equals(playerId)) {
                    String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(game.getTimestamp());
                    GameInfo gameInfo = new GameInfo(game.getId(), timestamp, game.getPlayers().get(0).getName());
                    result.add(gameInfo);
                    break;
                }
            }
        }
        return result;
    }

    public List<PlayerInfo> getPlayers() {
        return database.getPlayers();
    }

    public String getPlayerName(String playerId) {
        return database.getPlayerName(playerId);
    }

    public List<PlayerInfo> getPlayers(ArrayList<PlayerInfo> excludedPlayers) {
        return database.getPlayers(excludedPlayers);
    }

    public Game getGame(String id) {
        return database.getGame(id);
    }

    public boolean noPlayers () {
        return database.noPlayers();
    }

    public PlayerStats getPlayerStats(PlayerInfo playerInfo) {
        return database.getStats(playerInfo);
    }

    public int getGamesCount () {
        return database.getGamesCount();
    }

    public int getPlayersCount() {
        return database.getPlayersCount();
    }

    public int getTossesCount () {
        return database.getTossesCount();
    }

    public String getCreated () {
        if (database.getCreated() == 0) {
            firebaseManager.searchDatabase(databaseId, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    database.setCreated(Long.parseLong(s));
                    for (DatabaseListener listener : listeners)
                        listener.onDatabaseEvent(Event.CREATED_TIMESTAMP_ADDED);
                }
            }, null);
            return null;
        }
        return new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(database.getCreated());
    }

    public String getUpdated () {
        return new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(database.lastUpdated());
    }

}
