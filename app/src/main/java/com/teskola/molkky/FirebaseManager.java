package com.teskola.molkky;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;


public class FirebaseManager {

    private final FirebaseDatabase firebase = FirebaseDatabase.getInstance("https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseRef = firebase.getReference("databases");
    private final DatabaseReference usersRef = firebase.getReference("users");
    private DatabaseReference myDatabaseRef;
    private HashMap<String, DatabaseReference> myGamesRef = new HashMap<>();
    private HashMap<DatabaseReference, ChildEventListener> databaseListeners = new HashMap<>();
    private HashMap<String, ChildEventListener> gameListeners = new HashMap<>();
    private final Database database;
    private DatabaseListener listener;

    public interface DatabaseListener {
        void onNewUser();
        void onUserRemoved();
        void onGameAdded();
        void onGameRemoved();
    }

    public FirebaseManager(Database database, DatabaseListener listener) {
        this.listener = listener;
        this.database = database;
    }

    public FirebaseManager(String databaseId, Database database, DatabaseListener listener) {
        this.listener = listener;
        this.database = database;
        myDatabaseRef = firebase.getReference("databases/" + databaseId + "/users/");
        ChildEventListener childEventListener = databaseListener;
        databaseListeners.put(myDatabaseRef, childEventListener);
        myDatabaseRef.addChildEventListener(childEventListener);
    }

    private final ChildEventListener databaseListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String previousChildName) {
            DatabaseReference databaseReference = firebase.getReference("users/" + ds.getKey() + "/games");
            myGamesRef.put(ds.getKey(), databaseReference);
            ChildEventListener childEventListener = gamesListener(ds.getKey());
            gameListeners.put(ds.getKey(), childEventListener);
            databaseReference.addChildEventListener(childEventListener);
            listener.onNewUser();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            myGamesRef.get(snapshot.getKey()).removeEventListener(gameListeners.get(snapshot.getKey()));
            myGamesRef.remove(snapshot.getKey());
            database.removeDatabase(snapshot.getKey());
            listener.onUserRemoved();

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private ChildEventListener gamesListener(String databaseId) {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                database.addGame(databaseId, snapshot.getKey(), snapshot.getValue(Game.class));
                listener.onGameAdded();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                database.addGame(databaseId, snapshot.getKey(), snapshot.getValue(Game.class));
                listener.onGameAdded();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                database.removeGame(databaseId, snapshot.getKey());
                listener.onGameRemoved();

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    public static HashMap<String, String> addTimestamp() {
        HashMap<String, String> timestamp = new HashMap<>();
        timestamp.put(".sv", "timestamp");
        return timestamp;
    }

    public FirebaseManager searchDatabase (String database, OnSuccessListener<String> response, OnFailureListener error) {
        databaseRef.child(database).child("created").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(String.valueOf(task.getResult().getValue()));
            else error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager initializeDatabase(String database, OnSuccessListener<Void> response, OnFailureListener error) {
        databaseRef.child(database).child("created").setValue(addTimestamp()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                response.onSuccess(null);
            }
            else error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager addUser(String database, String uid, OnSuccessListener<Void> response, OnFailureListener error) {
        databaseRef.child(database).child("users").child(uid).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myDatabaseRef = firebase.getReference("databases/" + database + "/users/");
                ChildEventListener childEventListener = databaseListener;
                databaseListeners.put(myDatabaseRef, childEventListener);
                myDatabaseRef.addChildEventListener(childEventListener);
                response.onSuccess(null);
            }
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager removeUser(String db, String uid, OnSuccessListener<Void> response, OnFailureListener error) {
        myDatabaseRef.removeEventListener(databaseListeners.get(myDatabaseRef));
        databaseListeners.remove(myDatabaseRef);
        for (String key : myGamesRef.keySet()) {
            myGamesRef.get(key).removeEventListener(gameListeners.get(key));
            database.removeDatabase(key);
        }
        myGamesRef.clear();
        databaseRef.child(db).child("users").child(uid).setValue(null).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {


                response.onSuccess(null);
            }
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager addGameToDatabase(Game game, String user, OnSuccessListener<String> response, OnFailureListener error) {
        String gameId;
        if (game.getId() == null)
            gameId = usersRef.child(user).child("games").push().getKey();
        else
            gameId = game.getId();
        Date date = new Date();
        game.setTimestamp(date.getTime());
        usersRef.child(user).child("/games/" + gameId).setValue(game).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(gameId);
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }
}

