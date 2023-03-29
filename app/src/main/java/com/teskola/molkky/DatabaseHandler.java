package com.teskola.molkky;


import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.database.core.utilities.Pair;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DatabaseHandler implements FirebaseManager.DatabaseListener {

    public static final int DATABASE_ID_LENGTH = 6;     // only even numbers allowed
    public static final int LIVEGAME_ID_LENGTH = 4;
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
        for (DatabaseListener listener : listeners)
            listener.onDatabaseEvent(Event.DATABASE_USER_REMOVED);
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

    public boolean isConnected() {
        return firebaseManager != null;
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
            return "";
        }
        return uid.substring(0, DATABASE_ID_LENGTH).toLowerCase();
    }

    public static String getDatabaseId(String uid) {
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
        preferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        databaseId = preferences.getString("DATABASE", getShortId());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        if (FirebaseAuth.getInstance().getUid() == null)
            signIn();
        else
            firebaseManager = new FirebaseManager(FirebaseAuth.getInstance().getUid(), databaseId, this);
    }

    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }

    public void changeDatabase(String newDatabaseId) {
        if (firebaseManager == null) {
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

            firebaseManager.removeUser(databaseId,
                    res1 -> firebaseManager.addUser(newDatabaseId, res2 -> {
                        for (DatabaseListener listener : listeners)
                            listener.onDatabaseEvent(Event.DATABASE_CHANGED);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("DATABASE", newDatabaseId);
                        databaseId = newDatabaseId;
                        editor.apply();
                         // database.setCreated(Long.parseLong(response));
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
            firebaseManager = new FirebaseManager(FirebaseAuth.getInstance().getUid(), databaseId, this);
            firebaseManager.initializeDatabase(getShortId(), response -> {
                for (DatabaseListener listener : listeners)
                    listener.onDatabaseEvent(Event.DATABASE_CONNECTED);
            }, e -> {
                for (DatabaseListener listener : listeners)
                    listener.onError(Error.UNKNOWN_ERROR);
            }).addUser(getShortId(), response -> {
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
    }

    public void saveGame(Game game, List<Toss> tosses) {
        if (firebaseManager == null) {
            return;
        }

        firebaseManager.addGameToDatabase(game, tosses, response -> {
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

    public void updateTosses (List<Toss> tosses) {
        firebaseManager.setTosses(getLiveGameId(), tosses);
    }

    /*
    *
    *  Returns true, if player name is already in database else returns false.
    *
    * */

    public boolean addPlayer(PlayerInfo player) {
        String playerId = firebaseManager.getData().getPlayerId(player.getName());
        if (playerId != null) {
            player.setId(playerId);
            return true;
        } else {
            player.setId(UUID.randomUUID().toString().substring(0, 8));
            return false;
        }
    }

    public List<FirebaseManager.Data.Game> getGames() {
        return firebaseManager.getData().getGames();
    }

    public List<FirebaseManager.Data.Game> getGames(String playerId) {
        return firebaseManager.getData().getGames(playerId);
    }

    public List<PlayerInfo> getPlayers() {
        return firebaseManager.getData().getPlayers();
    }

    public List<PlayerInfo> getPlayers(List<PlayerInfo> excludedPlayers) {
        ArrayList<String> pids = new ArrayList<>();
        for (PlayerInfo player : excludedPlayers)
            pids.add(player.getId());
        return firebaseManager.getData().getPlayers(pids);
    }

    public boolean noPlayers () {
        return firebaseManager.getData().noPlayers();
    }

    public PlayerStats getPlayerStats(PlayerInfo playerInfo) {
        return firebaseManager.getData().getPlayerStats(playerInfo);
    }

    public int getGamesCount () {
        return firebaseManager.getData().getGamesCount();
    }

    public int getPlayersCount() {
        return firebaseManager.getData().getPlayersCount();
    }

    public int getTossesCount () {
        return firebaseManager.getData().getTossesCount();
    }

    public String getCreated () {
        if (firebaseManager == null) {
            return null;
        }
        if (firebaseManager.getData().created == 0) {
            return null;
        }
        return new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(firebaseManager.getData().created);
    }

    public String getUpdated () {
        if (firebaseManager.getData().updated == 0)
            return null;
        return new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(firebaseManager.getData().updated);
    }



}
