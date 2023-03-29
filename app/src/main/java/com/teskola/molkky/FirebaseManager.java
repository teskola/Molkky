package com.teskola.molkky;


import android.util.Pair;

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
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class FirebaseManager {

    private final FirebaseDatabase firebase = FirebaseDatabase.getInstance("https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference databaseRef = firebase.getReference("databases");
    private final DatabaseReference liveRef = firebase.getReference("livegames");
    private final DatabaseReference homeRef;
    private DatabaseReference myDatabaseRef;
    private final String uid;
    private final HashMap<String, DatabaseReference> myGamesRef = new HashMap<>();
    private final HashMap<String, ChildEventListener> gameListeners = new HashMap<>();
    private final HashMap<String, DatabaseReference> myPlayersRef = new HashMap<>();
    private final HashMap<String, ChildEventListener> playerListeners = new HashMap<>();
    private final DatabaseListener listener;
    private LiveGameListener liveGameListener;
    private final Data data;

    public interface DatabaseListener {
        void onNewUser();
        void onUserRemoved();
        void onGameAdded();
        void onGameRemoved();
    }

    public interface LiveGameListener {
        void onTossesChanges (List<Pair<String, Long>> tosses);
    }

    public FirebaseManager(String uid, String dbid, DatabaseListener listener) {
        this.listener = listener;
        this.uid = uid;
        data = new Data();
        myDatabaseRef = firebase.getReference("databases/" + dbid + "/users/");
        homeRef = firebase.getReference("users/" + uid);
        homeRef.addChildEventListener(gamesListener("home"));
        homeRef.addChildEventListener(playersListener("home"));
        myDatabaseRef.addChildEventListener(databaseListener);
    }

    public Data getData () {
        return this.data;
    }

    public static class Data {

       private final Map<String, String> names = new HashMap<>();
       private final Map<String, Map<String, Data.Game>> games = new HashMap<>();

       public Data () {}


        public static class Game implements Comparable<Game> {
            List<Pair<String, Long>> tosses;
            long timestamp;
            List<String> players;
            String winner;

            public Game() {}

            @NonNull
            @Override
            public String toString() {
                String timestampString = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(timestamp);
                return timestampString + " (" + winner + ")";

            }

            @Override
            public int compareTo(Game game) {
                return Long.compare(game.timestamp, this.timestamp);
            }
        }

        public void addGame (String dbid, String gid, Game game) {
           Objects.requireNonNull(games.get(dbid)).put(gid, game);
       }

        public void removeGame (String dbid, String gid) {
           games.get(dbid).remove(gid);
        }

        private void removePlayersWithNoGames () {
            List<String> removed = new ArrayList<>();
            for (String pid : names.keySet()) {
                removed.add(pid);

                for (Map<String,Game> database : games.values()) {
                    for (Game game : database.values()) {
                        if (game.players.contains(pid)) {
                            removed.remove(pid);
                            break;
                        }
                    }
                }
            }
            for (String id : removed) {
                names.remove(id);
            }
        }

        public void removeDatabase (String key) {
            games.remove(key);
            removePlayersWithNoGames();
        }

        public String getPlayerId (String name) {
           for (Map.Entry<String, String> entry : names.entrySet()) {
               if (name.equals(entry.getValue()))
                   return entry.getKey();
           }
           return null;
        }

        public List<Game> getGames() {
           Map<String, Game> allGames = new HashMap<>();
           for (String key : games.keySet()) {
               allGames.putAll(games.get(key));
           }
           List<Game> list = new ArrayList<>(allGames.values());
            Collections.sort(list);
            return list;
        }

        public List<Game> getGames(String pid) {
           List<Game> list = new ArrayList<>();
            for (Map<String, Game> database : games.values()) {
                for (Game game : database.values()) {
                    if (game.players.contains(pid)) {
                        list.add(game);
                    }
                }
            }
            Collections.sort(list);
            return list;
        }

        public List<PlayerInfo> getPlayers() {
           List<PlayerInfo> players = new ArrayList<>();
           for (String key : names.keySet()) {
               players.add(new PlayerInfo(key,names.get(key)));
           }
           return players;
        }

        public List<PlayerInfo> getPlayers(List<String> excludedPlayers) {
            List<PlayerInfo> players = new ArrayList<>();
            for (String key : names.keySet()) {
                PlayerInfo player = new PlayerInfo(key,names.get(key));
                players.add(player);
                for (String excluded : excludedPlayers) {
                    if (key.equals(excluded)) {
                        players.remove(player);
                    }
                }
            }
            return players;
        }

        public boolean noPlayers () {
          return names.size() == 0;
        }

    }



    private final ChildEventListener databaseListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String previousChildName) {
            if (!Objects.equals(ds.getKey(), uid)) {
                DatabaseReference gameRef = firebase.getReference("users/" + ds.getKey() + "/games");
                DatabaseReference playerRef = firebase.getReference("users/" + ds.getKey() + "/players");
                myGamesRef.put(ds.getKey(), gameRef);
                myPlayersRef.put(ds.getKey(), playerRef);
                ChildEventListener gListener = gamesListener(ds.getKey());
                ChildEventListener pListener = playersListener(ds.getKey());
                gameListeners.put(ds.getKey(), gListener);
                playerListeners.put(ds.getKey(), pListener);
                gameRef.addChildEventListener(gListener);
                playerRef.addChildEventListener(pListener);
                listener.onNewUser();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            // if (!myGamesRef.isEmpty() && myGamesRef.get(snapshot.getKey()) != null)
                Objects.requireNonNull(myGamesRef.get(snapshot.getKey())).removeEventListener(Objects.requireNonNull(gameListeners.get(snapshot.getKey())));
                Objects.requireNonNull(myPlayersRef.get(snapshot.getKey())).removeEventListener(Objects.requireNonNull(playerListeners.get(snapshot.getKey())));
                myGamesRef.remove(snapshot.getKey());
                myPlayersRef.remove(snapshot.getKey());
                data.removeDatabase(snapshot.getKey());
                listener.onUserRemoved();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private final ValueEventListener tossListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            liveGameListener.onTossesChanges((List<Pair<String, Long>>) snapshot.getValue());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private ChildEventListener playersListener(String databaseId) {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private ChildEventListener gamesListener(String databaseId) {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                data.addGame(databaseId, snapshot.getKey(), snapshot.getValue(Data.Game.class));
                listener.onGameAdded();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                data.addGame(databaseId, snapshot.getKey(), snapshot.getValue(Data.Game.class));
                listener.onGameAdded();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                data.removeGame(databaseId, snapshot.getKey());
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

    public void addUser(String database, OnSuccessListener<Void> response, OnFailureListener error) {
        databaseRef.child(database).child("users").child(uid).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myDatabaseRef = firebase.getReference("databases/" + database + "/users/");
                myDatabaseRef.addChildEventListener(databaseListener);
                response.onSuccess(null);
            }
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    public void removeUser(String db, OnSuccessListener<Void> response, OnFailureListener error) {
        //if (databaseListeners.get(myDatabaseRef) != null)
        myDatabaseRef.removeEventListener(databaseListener);
        for (String key : myGamesRef.keySet()) {
            // if (gameListeners.get(key) != null)
            myGamesRef.get(key).removeEventListener(gameListeners.get(key));
            myPlayersRef.get(key).removeEventListener(playerListeners.get(key));
            data.removeDatabase(key);
        }
        myGamesRef.clear();
        myPlayersRef.clear();
        databaseRef.child(db).child("users").child(uid).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                response.onSuccess(null);
            }
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    public void addGameToDatabase(Game game, List<Pair<String, Long>> tosses, OnSuccessListener<String> response, OnFailureListener error) {
        String gameId;
        if (game.getId() == null)
            gameId = homeRef.child("games").push().getKey();
        else
            gameId = game.getId();

        Map<String, Object> names = new HashMap<>(game.getPlayers().size());
        for (Player player : game.getPlayers()) {
            names.put(player.getId(), player.getName());
        }

        HashMap<String, Object> data = new HashMap<>(4);
        data.put("timestamp", ServerValue.TIMESTAMP);
        data.put("winner", game.getPlayer(0).getId());
        data.put("players", game.getPids());
        data.put("tosses", tosses);
        homeRef.child("/games/" + gameId).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                response.onSuccess(gameId);
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
        homeRef.child("players").updateChildren(names);

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

    public void getLiveGamePlayers (String gameId, OnSuccessListener<List<PlayerInfo>> response, OnFailureListener error) {
        liveRef.child(gameId).child("players").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot ds = task.getResult();
                if (ds.getValue() == null) {
                    error.onFailure(new Exception("game not found"));
                    return;
                }
                List<PlayerInfo> players = new ArrayList<>();
                for (DataSnapshot child : ds.getChildren()) {
                    PlayerInfo player = child.getValue(PlayerInfo.class);
                    players.add(player);
                }
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
        List<Object> tosses = new ArrayList<>();
        for (int i = 0; i < tossesCount; i++) {
            tosses.add(game.getPlayer(0).getUndoStack().peek());
            game.addToss(game.getPlayer(0).getUndoStack().pop());
        }
        if (tosses.size() > 0)
            liveRef.child(id + "/tosses/").setValue(tosses).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Map<String, Object> timestamp = new HashMap<>();
                        timestamp.put("updated", ServerValue.TIMESTAMP);
                        liveRef.child(id).updateChildren(timestamp);
                    }
                }
            });
    }

    public void setTosses (String id, List<Pair<String, Long>> tosses) {
        liveRef.child(id + "/tosses/").setValue(tosses).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> timestamp = new HashMap<>();
                timestamp.put("updated", ServerValue.TIMESTAMP);
                liveRef.child(id).updateChildren(timestamp);
            }
        });
    }

    public void setLiveGameListener (String id, LiveGameListener liveGameListener) {
        this.liveGameListener = liveGameListener;
        liveRef.child(id).child("tosses").addValueEventListener(tossListener);
    }

    public void removeLiveGameListener (String id) {
        this.liveGameListener = null;
        liveRef.child(id).child("tosses").removeEventListener(tossListener);
    }


}

