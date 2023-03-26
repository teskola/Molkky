package com.teskola.molkky;


import android.content.Context;
import android.content.SharedPreferences;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DatabaseHandler implements FirebaseManager.DatabaseListener {

    public static final int DATABASE_ID_LENGTH = 6;     // only even numbers allowed
    public static final int LIVEGAME_ID_LENGTH = 4;
    private final Database database;
    private final ArrayList<DatabaseListener> listeners = new ArrayList<>();
    private static DatabaseHandler instance;
    private FirebaseManager firebaseManager;
    private String databaseId;
    private final SharedPreferences preferences;

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
        SPECTATOR_MODE_AVAILABLE,
        CREATED_TIMESTAMP_ADDED,
        PLAYER_ADDED_FROM_DATABASE,
    }

    public enum Error {
        NETWORK_ERROR,
        UNKNOWN_ERROR,
        ADD_GAME_FAILED,
        SPECTATOR_MODE_UNAVAILABLE
    }

    public boolean isNotConnected() {
        return firebaseManager == null;
    }

    public void stop() {
        if (firebaseManager != null)
            firebaseManager.stop();
    }
    public void start() {
        if (firebaseManager != null)
            firebaseManager.start();
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
        return uid.substring(0, DATABASE_ID_LENGTH).toLowerCase();
    }

    /*
    *
    * Takes 4 last characters of user id as live game id.
    *
    * */

    public String getLiveGameId () {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            signIn();
            return null;
        }
        return uid.substring(uid.length() - LIVEGAME_ID_LENGTH).toLowerCase();
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

    public void changeDatabase(String newDatabaseId) {
        if (firebaseManager == null) {
            signIn();
            return;
        }
        firebaseManager.searchDatabase(newDatabaseId, response -> {
            if (response.equals("null")) {
                for (DatabaseListener listener : listeners)
                    listener.onDatabaseEvent(Event.DATABASE_NOT_FOUND);
                return;
            }
            for (DatabaseListener listener : listeners)
                listener.onDatabaseEvent(Event.DATABASE_FOUND);

            firebaseManager.removeUser(databaseId, FirebaseAuth.getInstance().getUid(),
                    res1 -> firebaseManager.addUser(newDatabaseId, FirebaseAuth.getInstance().getUid(), res2 -> {
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

        FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(authResult -> {
            firebaseManager = new FirebaseManager(database, this);
            firebaseManager.initializeDatabase(getShortId(), response -> {
                for (DatabaseListener listener : listeners)
                    listener.onDatabaseEvent(Event.DATABASE_CONNECTED);
            }, e -> {
                for (DatabaseListener listener : listeners)
                    listener.onError(Error.UNKNOWN_ERROR);
            }).addUser(getShortId(), FirebaseAuth.getInstance().getUid(), response -> {
                databaseId = getShortId();
                for (DatabaseListener listener : listeners)
                    listener.onDatabaseEvent(Event.DATABASE_CREATED);
            }, e -> {
                for (DatabaseListener listener : listeners)
                    listener.onError(Error.UNKNOWN_ERROR);
            });
        }).addOnFailureListener(e -> {
            for (DatabaseListener listener : listeners)
                listener.onError(Error.NETWORK_ERROR);
        });

        /*FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener
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
                });*/
    }

    public void saveGame(Game game) {
        if (firebaseManager == null) {
            signIn();
            return;
        }
        firebaseManager.addGameToDatabase(game, FirebaseAuth.getInstance().getUid(), response -> {
            game.setId(response);
            for (DatabaseListener listener : listeners)
                listener.onDatabaseEvent(Event.GAME_ADDED);
        }, e -> {
            for (DatabaseListener listener : listeners)
                listener.onError(Error.ADD_GAME_FAILED);
        });
    }

    public void startGame(Game game) {
        if (firebaseManager == null) {
            signIn();
            return;
        }
        firebaseManager.addLiveGame(getLiveGameId(), game, response -> {
            for (DatabaseListener listener : listeners)
                listener.onDatabaseEvent(Event.SPECTATOR_MODE_AVAILABLE);
        }, e -> {
            for (DatabaseListener listener : listeners)
                listener.onError(Error.SPECTATOR_MODE_UNAVAILABLE);
        });
    }

    public void addToss(int count, int value) {
        if (firebaseManager == null)
            return;
        firebaseManager.addToss(getLiveGameId(), count, value);
    }

    public void removeToss(int count) {
        if (firebaseManager == null)
            return;
        firebaseManager.removeToss(getLiveGameId(), count);
    }

    /*
    *
    *  Returns true, if player name is already in database else returns false.
    *
    * */

    public boolean addPlayer(PlayerInfo player) {
        String playerId = database.getPlayerId(player.getName());
        if (playerId != null) {
            player.setId(playerId);
            return true;
        } else {
            player.setId(UUID.randomUUID().toString().substring(0, 8));
            return false;
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

    public List<PlayerInfo> getPlayers(List<PlayerInfo> excludedPlayers) {
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
        if (firebaseManager == null) {
            signIn();
            return null;
        }
        if (database.getCreated() == 0) {
            firebaseManager.searchDatabase(databaseId, response -> {
                database.setCreated(Long.parseLong(response));
                for (DatabaseListener listener : listeners)
                    listener.onDatabaseEvent(Event.CREATED_TIMESTAMP_ADDED);
            }, e -> {
                for (DatabaseListener listener : listeners)
                    listener.onError(Error.UNKNOWN_ERROR);
            });
            return null;
        }
        return new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(database.getCreated());
    }

    public String getUpdated () {
        if (database.lastUpdated() == 0)
            return null;
        return new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(database.lastUpdated());
    }

}
