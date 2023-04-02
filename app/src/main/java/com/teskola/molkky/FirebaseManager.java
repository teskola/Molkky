package com.teskola.molkky;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class FirebaseManager {

    public static final int DATABASE_ID_LENGTH = 6;     // only even numbers allowed
    public static final int LIVEGAME_ID_LENGTH = 4;

    private static FirebaseManager instance;
    private final FirebaseDatabase firebase = FirebaseDatabase.getInstance("https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference databaseRef;
    private final Map<String, DatabaseReference> userRefs = new HashMap<>(1);
    private final DatabaseReference liveRef = firebase.getReference("livegames");
    private PlayersByGameListener playersByGameListener;
    private NamesListener namesListener;
    private MetaListener metaListener;
    private LiveGameListener liveGameListener;

    private final SharedPreferences preferences;
    private String uid;
    private String dbid;
    public static class MetaData {
        private long timestamp;
        private long tossCount;
        private String winner;

        public MetaData() {}

        public long getTimestamp() {
            return timestamp;
        }

        public long getTossCount() {
            return tossCount;
        }

        public String getWinner() {
            return winner;
        }
    }

    private final ChildEventListener usersListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String previousChildName) {
            userRefs.put(ds.getKey(), firebase.getReference("users").child(Objects.requireNonNull(ds.getKey())));
            if (metaListener != null) {
                Objects.requireNonNull(userRefs.get(ds.getKey())).addValueEventListener(metaGamesListener(ds.getKey()));
                Objects.requireNonNull(userRefs.get(ds.getKey())).addValueEventListener(metaPlayersListener(ds.getKey()));
            }
            if (namesListener != null) {
                Objects.requireNonNull(userRefs.get(ds.getKey())).addValueEventListener(namesListener(ds.getKey()));
            }

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            if (Objects.equals(snapshot.getKey(), uid))
                return;
            if (metaListener != null) {
                Objects.requireNonNull(userRefs.get(snapshot.getKey())).removeEventListener(metaGamesListener(snapshot.getKey()));
                Objects.requireNonNull(userRefs.get(snapshot.getKey())).removeEventListener(metaPlayersListener(snapshot.getKey()));
                metaListener.onDatabaseRemoved(snapshot.getKey());
            }
            if (namesListener != null) {
                Objects.requireNonNull(userRefs.get(snapshot.getKey())).removeEventListener(namesListener(snapshot.getKey()));
                namesListener.onDatabaseRemoved(snapshot.getKey());
            }
            userRefs.remove(snapshot.getKey());

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
            liveGameListener.onTossesChanges((List<Toss>) snapshot.getValue());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private ValueEventListener metaGamesListener(String key) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<MetaData> metaDataSet = new HashSet<>();
                for (DataSnapshot game : snapshot.getChildren()) {
                    MetaData metaData = game.getValue(MetaData.class);
                    metaDataSet.add(metaData);
                }
                Map<String, Set<MetaData>> metaDataMap = new HashMap<>();
                metaDataMap.put(key, metaDataSet);
                metaListener.onGamesReceived(metaDataMap);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }

    private ValueEventListener metaPlayersListener(String key) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> playerSet = new HashSet<>();
                for (DataSnapshot player : snapshot.getChildren()) {
                    String pid = player.getValue(String.class);
                    playerSet.add(pid);
                }
                Map<String, Set<String>> playerMap = new HashMap<>();
                playerMap.put(key, playerSet);
                metaListener.onPlayersReceived(playerMap);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }

    private ValueEventListener namesListener(String key) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<PlayerInfo> playerSet = new HashSet<>();
                for (DataSnapshot player : snapshot.getChildren()) {
                    playerSet.add(new PlayerInfo(player.getKey(), player.getValue(String.class)));
                }
                Map<String, Set<PlayerInfo>> playerMap = new HashMap<>();
                playerMap.put(key, playerSet);
                namesListener.onPlayersReceived(playerMap);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }

    private ChildEventListener playersListener(String user) {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (data.names.containsKey(snapshot.getKey()))
                    return;
                String name = (String) snapshot.getValue();
                int i = 1;
                while (data.names.containsValue(name)) {
                    name += i;
                }
                data.names.put(snapshot.getKey(), name);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (user.equals(uid))
                    data.names.put(snapshot.getKey(), (String) snapshot.getValue());
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
                Data.Game game = snapshot.getValue(Data.Game.class);
                data.addGame(databaseId, snapshot.getKey(), game);
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

    public interface NamesListener {
        void onPlayersReceived(Map<String, Set<PlayerInfo>> players);
        void onDatabaseRemoved(String id);
    }

    public interface MetaListener {
        void onGamesReceived(Map<String, Set<MetaData>> data);
        void onPlayersReceived(Map<String, Set<String>> players);
        void onDatabaseRemoved(String id);

    }

    public interface PlayersByGameListener {
        void onDataReceived(Map<String, List<String>> data);
        void onDatabaseRemoved(String id);
    }

    public interface LiveGameListener {
        void onTossesChanges (List<Toss> tosses);
    }

    public static FirebaseManager getInstance(Context context) {
        if (instance == null)
            instance = new FirebaseManager(context.getApplicationContext());
        return instance;
    }

    private FirebaseManager (Context context) {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null)
            signIn();
        preferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        dbid = preferences.getString("DATABASE", getShortId());
        addUser(dbid, null, null);
    }

    private void updateSharedPreferences(String newDatabase) {
        dbid = newDatabase;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("DATABASE", newDatabase);
        editor.apply();
    }

    public void registerNamesListener (NamesListener namesListener) {
        this.namesListener = namesListener;
        fetchNames();
    }

    public void unRegisterNamesListener() {
        for (String key : userRefs.keySet())
            userRefs.get(key).removeEventListener(namesListener(key));
        this.namesListener = null;
    }

    public void registerMetaListener(MetaListener metaListener) {
        this.metaListener = metaListener;
        fetchMetaGameData();
        fetchMetaPlayerData();
    }

    public void unregisterMetaListener() {
        for (String key : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(key)).removeEventListener(metaGamesListener(key));
            Objects.requireNonNull(userRefs.get(key)).removeEventListener(metaPlayersListener(key));
        }
        this.metaListener = null;
    }

    public String getShortId() {
        if (uid == null) {
            return null;
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
            return null;
        }
        return uid.substring(uid.length() - LIVEGAME_ID_LENGTH).toLowerCase();
    }


    public void stop() {
        FirebaseDatabase.getInstance().goOffline();
    }
    public void start() {
        FirebaseDatabase.getInstance().goOnline();
    }

    public void signIn() {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                uid = FirebaseAuth.getInstance().getUid();
                initializeDatabase();
            }

        });
    }

    public void fetchCreated(OnSuccessListener<Long> response, OnFailureListener failureListener) {
        databaseRef.child("created").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getValue() == null)
                    databaseRef.child("created").setValue(ServerValue.TIMESTAMP);
                response.onSuccess(task.getResult().getValue(Long.class));
            }
            else
                failureListener.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    private void fetchMetaGameData() {
        for (String key : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(key)).child("games/meta").addValueEventListener(metaGamesListener(key));
        }
    }

    private void fetchMetaPlayerData() {
        for (String key : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(key)).child("players").addValueEventListener(metaPlayersListener(key));
        }
    }

    private void fetchNames() {
        for (String key : userRefs.keySet())
            Objects.requireNonNull(userRefs.get(key)).child("names").addValueEventListener(namesListener(key));
    }

    public void searchDatabaseId (String database, OnSuccessListener<Boolean> databaseFound, OnFailureListener failure) {
        firebase.getReference("databases/" + database + "/created").get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                databaseFound.onSuccess(task.getResult().exists());
            else
                failure.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    private void initializeDatabase() {
        firebase.getReference("databases/" + getShortId() + "/created").setValue(ServerValue.TIMESTAMP);
    }

    public void addUser(String database, OnSuccessListener<Void> onSuccessListener, OnFailureListener error) {
        if (!database.equals(dbid))
            updateSharedPreferences(database);
        databaseRef = firebase.getReference("databases").child(database);
        databaseRef.child("users").addChildEventListener(usersListener);
        databaseRef.child("users/" + uid).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onSuccessListener.onSuccess(null);
            }
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    public void removeUser(OnSuccessListener<Void> onSuccessListener, OnFailureListener error) {

        databaseRef.child("users/" + uid).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userRefs.clear();
                databaseRef.child("users").removeEventListener(usersListener);
                onSuccessListener.onSuccess(null);
            }
            else {
                error.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void addGameToDatabase(Game game) {

        DatabaseReference homeRef = firebase.getReference("users/" + uid);

        // get game id

        if (game.getId() == null)
            game.setId(homeRef.child("games").push().getKey());

        // update names and game ids for players

        Map<String, Object> names = new HashMap<>(game.getPlayers().size());
        for (Player player : game.getPlayers()) {
            homeRef.child("players").child(player.getId()).child(game.getId()).setValue(true);
            names.put(player.getId(), player.getNameInDatabase());
        }
        homeRef.child("names").updateChildren(names);

        // game metadata

        HashMap<String, Object> metaData = new HashMap<>(3);
        metaData.put("timestamp", ServerValue.TIMESTAMP);
        metaData.put("tossCount", (long) game.getTossesCount());
        metaData.put("winner", game.getPlayer(0).getId());
        homeRef.child("games").child("meta").child(game.getId()).setValue(metaData);

        // player ids

        List<Object> pids = new ArrayList<>(game.getPlayers().size());
        for (Player player : game.getPlayers())
            pids.add(player.getId());
        homeRef.child("games").child("players").child(game.getId()).setValue(pids);

        // tosses

        for (Player player : game.getPlayers())
            homeRef.child("tosses").child(game.getId()).child(player.getId()).setValue(player.getTosses());

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

    public void setTosses (String id, List<Toss> tosses) {
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

