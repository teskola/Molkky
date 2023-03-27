package com.teskola.molkky;


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
import com.google.firebase.database.ServerValue;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class FirebaseManager {

    private final FirebaseDatabase firebase = FirebaseDatabase.getInstance("https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseRef;
    private final DatabaseReference usersRef;
    private final DatabaseReference liveRef;
    private DatabaseReference myDatabaseRef;
    private final HashMap<String, DatabaseReference> myGamesRef = new HashMap<>();
    private final HashMap<DatabaseReference, ChildEventListener> databaseListeners = new HashMap<>();
    private final HashMap<String, ChildEventListener> gameListeners = new HashMap<>();
    private final Database database;
    private final DatabaseListener listener;
    private LiveGameListener liveGameListener;

    public interface DatabaseListener {
        void onNewUser();
        void onUserRemoved();
        void onGameAdded();
        void onGameRemoved();
    }

    public interface LiveGameListener {
        void onTossAdded(int value);
        void onTossRemoved(int count);
    }

    public FirebaseManager(Database database, DatabaseListener listener) {
        this.listener = listener;
        this.database = database;
        databaseRef = firebase.getReference("databases");
        usersRef = firebase.getReference("users");
        liveRef = firebase.getReference("livegames");
    }

    public FirebaseManager(String databaseId, Database database, DatabaseListener listener) {
        this.listener = listener;
        this.database = database;
        databaseRef = firebase.getReference("databases");
        usersRef = firebase.getReference("users");
        liveRef = firebase.getReference("livegames");
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
            // if (!myGamesRef.isEmpty() && myGamesRef.get(snapshot.getKey()) != null)
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

    private final ChildEventListener tossListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            liveGameListener.onTossAdded((int) snapshot.getValue());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            liveGameListener.onTossRemoved(Integer.valueOf(snapshot.getKey()));
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

    public void stop() {
        FirebaseDatabase.getInstance().goOffline();
    }
    public void start() {
        FirebaseDatabase.getInstance().goOnline();
    }

    public void searchDatabase (String database, OnSuccessListener<String> response, OnFailureListener error) {
        databaseRef.child(database).child("created").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(String.valueOf(task.getResult().getValue()));
            else error.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    public FirebaseManager initializeDatabase(String database, OnSuccessListener<Void> response, OnFailureListener error) {
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("created", ServerValue.TIMESTAMP);
        databaseRef.child(database).setValue(timestamp).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                response.onSuccess(null);
            }
            else error.onFailure(Objects.requireNonNull(task.getException()));
        });
        return this;
    }

    public void addUser(String database, String uid, OnSuccessListener<Void> response, OnFailureListener error) {
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
    }

    public void removeUser(String db, String uid, OnSuccessListener<Void> response, OnFailureListener error) {
        //if (databaseListeners.get(myDatabaseRef) != null)
            myDatabaseRef.removeEventListener(databaseListeners.get(myDatabaseRef));
        databaseListeners.remove(myDatabaseRef);
        for (String key : myGamesRef.keySet()) {
            // if (gameListeners.get(key) != null)
                myGamesRef.get(key).removeEventListener(gameListeners.get(key));
            database.removeDatabase(key);
        }
        myGamesRef.clear();
        databaseRef.child(db).child("users").child(uid).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                response.onSuccess(null);
            }
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    public void addGameToDatabase(Game game, String user, OnSuccessListener<String> response, OnFailureListener error) {
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
    }

    public void getLiveGames (OnSuccessListener<HashMap<String, Game>> response, OnFailureListener error) {
        liveRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HashMap<String, Game> games = new HashMap<>();
                Collection<DataSnapshot> gamesDs = (Collection<DataSnapshot>) task.getResult().getChildren();
                for (DataSnapshot dsGame : gamesDs) {
                    Player[] players = (Player[]) dsGame.child("players").getValue(PlayerInfo[].class);
                    Game game = new Game(Arrays.asList(players));
                    if (dsGame.hasChild("tosses")) {
                        int tossesSize = (int) dsGame.child("tosses").getChildrenCount() - 1; // -1 for timestamp
                        for (int i=0; i < tossesSize; i++)
                            game.addToss((int) dsGame.child("tosses").child(String.valueOf(i)).getValue());
                    }
                    games.put(dsGame.getKey(), game);
                }
                response.onSuccess(games);
            } else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    public void getLiveGamePlayers (String gameId, OnSuccessListener<PlayerInfo[]> response, OnFailureListener error) {
        liveRef.child(gameId).child("players").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot ds = task.getResult();
                if (!Objects.equals(ds.getValue(), "null")) {
                    error.onFailure(new Exception("game not found"));
                    return;
                }
                PlayerInfo[] players = ds.getValue(PlayerInfo[].class);
                response.onSuccess(players);
            }
            else {
                error.onFailure(task.getException());
            }
        });
    }


    public void addLiveGame (String id, Game game, OnSuccessListener<String> response, OnFailureListener error) {
        int tossesCount = game.getTossesCount();
        while (game.getTossesCount() > 0) {
            for (int i = 1; i < game.getPlayers().size(); i++) {
                Player previous = game.getPlayer(game.getPlayers().size() - i);
                Player current = game.getPlayer(0);
                if ((previous.getTosses().size() > current.getTosses().size()) || !previous.isEliminated()) {
                    previous.getUndoStack().push(previous.removeToss());
                    game.setTurn(game.getPlayers().size() - i);
                    break;
                }
            }
        }
        Map<String, Object> players = new HashMap<>();
        players.put("players", game.getPlayers());
        liveRef.child(id).updateChildren(players).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                response.onSuccess(id);
                Map<String, Object> timestamp = new HashMap<>();
                timestamp.put("started", ServerValue.TIMESTAMP);
                liveRef.child(id).updateChildren(timestamp);
            } else
                error.onFailure(task.getException());
        });
        Map<String, Object> tosses = new HashMap<>();
        for (int i = 0; i < tossesCount; i++) {
            tosses.put(String.valueOf(i), (long) game.getPlayer(0).getUndoStack().peek());
            game.addToss(game.getPlayer(0).getUndoStack().pop());
        }
        if (tosses.size() > 0)
            liveRef.child(id + "/tosses/").updateChildren(tosses);
    }

    public void addToss (String id, int count, int value) {
        Map<String, Object> toss = new HashMap<>();
        Map<String, Object> timestamp = new HashMap<>();
        toss.put(String.valueOf(count), (long) value);
        timestamp.put("lastUpdate", ServerValue.TIMESTAMP);
        liveRef.child(id).child("tosses").updateChildren(toss);
        liveRef.child(id).child("tosses").updateChildren(timestamp);
    }
    public void removeToss (String id, int count) {
        Map<String , Object> timestamp = new HashMap<>();
        timestamp.put("lastUpdate", ServerValue.TIMESTAMP);
        liveRef.child(id).child("tosses").child(String.valueOf(count)).removeValue();
        liveRef.child(id).child("tosses").updateChildren(timestamp);
    }

    public void setLiveGameListener (String id, LiveGameListener liveGameListener) {
        this.liveGameListener = liveGameListener;
        liveRef.child(id).child("tosses").addChildEventListener(tossListener);
    }

    public void removeLiveGameListener (String id) {
        this.liveGameListener = null;
        liveRef.child(id).child("tosses").removeEventListener(tossListener);
    }


}

