package com.teskola.molkky;


import android.content.Context;


import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MetaHandler implements FirebaseManager.MetaGamesListener, FirebaseManager.MetaPlayersListener, FirebaseAuth.AuthStateListener {

    private final DatabaseListener databaseListener;
    private final FirebaseManager firebaseManager;
    private Map<String, Set<FirebaseManager.MetaData>> allGames;
    private Map<String, Set<String>> allPlayers;

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getUid() != null) {
            getCreated();
            firebaseManager.registerMetaGamesListener(this);
            firebaseManager.registerMetaPlayersListener(this);
            FirebaseAuth.getInstance().removeAuthStateListener(this);
        }
    }

    @Override
    public void onGamesReceived(Map<String, Set<FirebaseManager.MetaData>> data) {
        allGames.putAll(data);
        sendGamesData();
    }

    @Override
    public void onPlayersReceived(Map<String, Set<String>> players) {
        allPlayers.putAll(players);
        sendPlayersData();
    }

    @Override
    public void onDatabaseRemoved(String key) {
        allGames.remove(key);
        sendGamesData();
        allPlayers.remove(key);
        sendPlayersData();
    }

    public interface DatabaseListener {
        void onError(Error error);
        void onDatabaseEvent(Event event);

        void onGamesReceived(int count);
        void onPlayersReceived (int count);
        void onTossesReceived (int count);
        void onUpdatedReceived (String date);
        void onCreatedReceived (String date);
    }


    public enum Event {
        DATABASE_FOUND,
        DATABASE_NOT_FOUND,
    }

    public enum Error {
        NETWORK_ERROR,
        UNKNOWN_ERROR,
    }

    public MetaHandler(Context context, DatabaseListener databaseListener) {
        clear();
        this.databaseListener = databaseListener;
        firebaseManager = FirebaseManager.getInstance(context);
        if (FirebaseAuth.getInstance().getUid() == null) {
            databaseListener.onError(Error.NETWORK_ERROR);
            FirebaseAuth.getInstance().addAuthStateListener(this);
        }
        else {
            firebaseManager.registerMetaGamesListener(this);
            firebaseManager.registerMetaPlayersListener(this);
            getCreated();
        }
    }

    private void clear() {
        allGames = new HashMap<>();
        allPlayers = new HashMap<>();
    }

    public void close() {
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        firebaseManager.unRegisterMetaGamesListener(this);
        firebaseManager.unregisterMetaPlayersListener();
    }

    private void sendPlayersData () {
        Set<String> players = new HashSet<>();
        for (String dbid : allPlayers.keySet()) {
            players.addAll(Objects.requireNonNull(allPlayers.get(dbid)));
        }
        databaseListener.onPlayersReceived(players.size());
    }

    private void sendGamesData () {
        long timestamp = 0;
        int tossCount = 0;
        int games = 0;
        for (String dbid : allGames.keySet()) {
            for (FirebaseManager.MetaData metaData : Objects.requireNonNull(allGames.get(dbid))) {
                timestamp = Math.max(timestamp, metaData.getTimestamp());
                tossCount += metaData.getTossCount();
                games++;
            }
        }
        databaseListener.onGamesReceived(games);
        databaseListener.onTossesReceived(tossCount);
        if (timestamp != 0) {
            String updatedString = new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(timestamp);
            databaseListener.onUpdatedReceived(updatedString);
        }
    }

    public void changeDatabase(String newDatabaseId) {

        if (FirebaseAuth.getInstance().getUid() == null) {
            databaseListener.onError(Error.NETWORK_ERROR);
            return;
        }

        firebaseManager.searchDatabaseId(newDatabaseId, databaseFound -> {
            if (databaseFound) {
                databaseListener.onDatabaseEvent(Event.DATABASE_FOUND);
                firebaseManager.unRegisterMetaGamesListener(this);
                firebaseManager.unregisterMetaPlayersListener();
                firebaseManager.removeUser(then -> firebaseManager.addUser
                        (newDatabaseId, next -> {
                            clear();
                            firebaseManager.registerMetaGamesListener(this);
                            firebaseManager.registerMetaPlayersListener(this);
                            getCreated();
                                } ,
                                error -> databaseListener.onError(Error.UNKNOWN_ERROR)),
                        error -> databaseListener.onError(Error.UNKNOWN_ERROR));
            }
            else
                databaseListener.onDatabaseEvent(Event.DATABASE_NOT_FOUND);
        }, error -> databaseListener.onError(Error.UNKNOWN_ERROR));
    }

    private void getCreated () {
        if (FirebaseAuth.getInstance().getUid() == null) {
            databaseListener.onError(Error.NETWORK_ERROR);
            return;
        }
        firebaseManager.fetchCreated(timestamp -> {
            if (timestamp == null)
                return;
            String timestampString = new SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(timestamp);
            databaseListener.onCreatedReceived(timestampString);
        }, error -> databaseListener.onError(Error.UNKNOWN_ERROR));
    }
}
