package com.teskola.molkky;



import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;


public class FirebaseManager {

    private final FirebaseDatabase firebase = FirebaseDatabase.getInstance("https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseRef = firebase.getReference("databases");
    private final DatabaseReference usersRef = firebase.getReference("users");
    private DatabaseReference myDatabaseRef;
    private HashMap<String, DatabaseReference> myGamesRef = new HashMap<>();
    private HashMap<String, DatabaseReference> myPlayersRef = new HashMap<>();
    private Database database = new Database();
    // private HashMap<String, HashMap<String, Game>> games = new HashMap<>();
    // private HashMap<String, PlayerInfo> playersMap = new HashMap<>();


    private final ChildEventListener databaseListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String previousChildName) {
            DatabaseReference databaseReference = firebase.getReference("users/" + ds.getKey() + "/games");
            myGamesRef.put(ds.getKey(), databaseReference);
            databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, Game> gamesMap = new HashMap<>();
                        DataSnapshot snapshot1 = task.getResult();
                        for (DataSnapshot game : snapshot1.getChildren()) {
                            gamesMap.put(game.getKey(), game.getValue(Game.class));
                        }
                        // games.put(ds.getKey(), gamesMap);
                        database.addGames(ds.getKey(), gamesMap);
                    }
                }
            });
            databaseReference.addChildEventListener(gamesListener(ds));

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            myGamesRef.get(snapshot.getKey()).removeEventListener(this);
            myPlayersRef.get(snapshot.getKey()).removeEventListener(this);
            // games.remove(snapshot.getKey());
            database.removeDatabase(snapshot.getKey());
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    public FirebaseManager() {
    }

    public FirebaseManager(String databaseId) {
        myDatabaseRef = firebase.getReference("databases/" + databaseId + "/users/");
        myDatabaseRef.addChildEventListener(databaseListener);
        getDatabaseAndSetListeners();
    }

    private void getDatabaseAndSetListeners () {
        myDatabaseRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        DatabaseReference gamesRef = firebase.getReference("users/" + ds.getKey() + "/games");
                        myGamesRef.put(ds.getKey(), gamesRef);
                        gamesRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    HashMap<String, Game> gamesMap = new HashMap<>();
                                    DataSnapshot snapshot1 = task.getResult();
                                    for (DataSnapshot game : snapshot1.getChildren()) {
                                        gamesMap.put(game.getKey(), game.getValue(Game.class));
                                    }
                                    // games.put(ds.getKey(), gamesMap);
                                    database.addGames(ds.getKey(), gamesMap);

                                }
                            }
                        });
                        gamesRef.addChildEventListener(gamesListener(ds));
                        DatabaseReference playersRef = firebase.getReference("users/" + ds.getKey() + "/players");
                        myPlayersRef.put(ds.getKey(), playersRef);
                        playersRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DataSnapshot snapshot1 = task.getResult();
                                    if (snapshot1.exists()) {
                                        for (DataSnapshot player : snapshot1.getChildren()) {
                                           // playersMap.put(player.getKey(), player.getValue(PlayerInfo.class));
                                            database.addPlayer(player.getKey(), player.getValue(PlayerInfo.class));
                                        }
                                    }
                                }
                            }
                        });
                        playersRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                database.addPlayer(snapshot.getKey(), snapshot.getValue(PlayerInfo.class));
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                database.removePlayer(snapshot.getKey());
                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }
        });
    }

    private ChildEventListener gamesListener(DataSnapshot ds) {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                database.addGame(ds.getKey(), snapshot.getKey(), snapshot.getValue(Game.class));
  /*              if (games.containsKey(ds.getKey()))
                    games.get(ds.getKey()).put(snapshot.getKey(), snapshot.getValue(Game.class));
                else {
                    HashMap<String, Game> newMap = new HashMap<>();
                    newMap.put(snapshot.getKey(), snapshot.getValue(Game.class));
                    games.put(ds.getKey(), newMap);
                }*/

                // TODO hae pelaajat, tarkista ovatko jo lisätty ja lisää

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                database.addGame(ds.getKey(), snapshot.getKey(), snapshot.getValue(Game.class));
                // games.get(ds.getKey()).put(snapshot.getKey(), snapshot.getValue(Game.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                database.removeGame(ds.getKey(), snapshot.getKey());
                // games.get(ds.getKey()).remove(snapshot.getKey());

                // TODO tarkista pelaajat ja poista tarvittaessa
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
                myDatabaseRef.addChildEventListener(databaseListener);
                response.onSuccess(null);
            }
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public FirebaseManager removeUser(String database, String uid, OnSuccessListener<Void> response, OnFailureListener error) {
        databaseRef.child(database).child("users").child(uid).setValue(null).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myDatabaseRef.removeEventListener(databaseListener);
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

    public FirebaseManager addPlayerToDatabase (PlayerInfo player, String user, OnSuccessListener<String> response, OnFailureListener error) {
        String playerId;
        if (player.getId() == null)
            playerId = usersRef.child(user).child("players").push().getKey();
        else
            playerId = player.getId();
        usersRef.child(user).child("/players/" + playerId).setValue(player).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(playerId);
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
     return this;
    }

    public FirebaseManager removePlayerFromDatabase(PlayerInfo player, String user) {

        usersRef.child(user).child("/players/" + player.getId()).setValue(null);
        return this;
    }

    public String getPlayerId (String name) {
        return database.getPlayerId(name);
    }

}

