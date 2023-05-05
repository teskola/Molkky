package com.teskola.molkky;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
    private StatsListener statsListener;
    private GamesListener gamesListener;
    private NamesListener namesListener;
    private List<MetaGamesListener> metaGamesListeners = new ArrayList<>();
    private MetaPlayersListener metaPlayersListener;
    private LiveGameListener liveGameListener;

    private final SharedPreferences preferences;
    private final SharedPreferences alterEgos;
    private String uid;
    private String dbid;
    public static class MetaData {
        private String id;
        private long timestamp;
        private long tossCount;
        private String winner;

        public MetaData() {}

        public void setId(String id) {this.id = id;}

        public String getId() { return  id;}

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
            if (metaGamesListeners != null && !metaGamesListeners.isEmpty())
                Objects.requireNonNull(userRefs.get(ds.getKey())).child("games/meta").addValueEventListener(metaGamesListener(ds.getKey()));
            if (metaPlayersListener != null)
                Objects.requireNonNull(userRefs.get(ds.getKey())).child("players").addValueEventListener(metaPlayersListener(ds.getKey()));
            if (namesListener != null)
                Objects.requireNonNull(userRefs.get(ds.getKey())).child("names").addValueEventListener(namesListener(ds.getKey()));
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            if (Objects.equals(snapshot.getKey(), uid))
                return;
            for (MetaGamesListener metaGamesListener : metaGamesListeners) {
                Objects.requireNonNull(userRefs.get(snapshot.getKey())).removeEventListener(metaGamesListener(snapshot.getKey()));
                metaGamesListener.onDatabaseRemoved(snapshot.getKey());
            }
            if (metaPlayersListener != null) {
                Objects.requireNonNull(userRefs.get(snapshot.getKey())).removeEventListener(metaPlayersListener(snapshot.getKey()));
                metaPlayersListener.onDatabaseRemoved(snapshot.getKey());
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

    private final ValueEventListener liveGameEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            liveGameListener.onLiveGameChange(snapshot.getValue(GameHandler.LiveGame.class));
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
                    Objects.requireNonNull(metaData).setId(game.getKey());
                    metaDataSet.add(metaData);
                }
                Map<String, Set<MetaData>> metaDataMap = new HashMap<>();
                metaDataMap.put(key, metaDataSet);
                for (MetaGamesListener metaGamesListener : metaGamesListeners)
                    metaGamesListener.onGamesReceived(metaDataMap);
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
                    playerSet.add(player.getKey());
                }
                Map<String, Set<String>> playerMap = new HashMap<>();
                playerMap.put(key, playerSet);
                if (metaPlayersListener != null)
                    metaPlayersListener.onPlayersReceived(playerMap);
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

    public interface NamesListener {
        void onPlayersReceived(Map<String, Set<PlayerInfo>> players);
        void onDatabaseRemoved(String id);
    }

    public interface MetaGamesListener {
        void onGamesReceived(Map<String, Set<MetaData>> data);
        void onDatabaseRemoved(String id);
    }

    public interface MetaPlayersListener {
        void onPlayersReceived(Map<String, Set<String>> players);
        void onDatabaseRemoved(String id);

    }

    public interface GamesListener {
        void onGameReceived(SavedGamesActivity.GameInfo gameInfo);
    }

    public interface StatsListener {
        void onGamesReceived(Map<String, Map<String, Map<String, Boolean>>> data);
    }

    public interface LiveGameListener {
        void onLiveGameChange (GameHandler.LiveGame liveGame);
    }

    public static FirebaseManager getInstance(Context context) {
        if (instance == null)
            instance = new FirebaseManager(context.getApplicationContext());
        return instance;
    }

    private FirebaseManager (Context context) {
        preferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        alterEgos = context.getSharedPreferences("ALTER_EGOS", Context.MODE_PRIVATE);
        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null)
            signIn();
        dbid = preferences.getString("DATABASE", getShortId());
        addUser(dbid, null, null);
    }

    private void updateSharedPreferences(String newDatabase) {
        dbid = newDatabase;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("DATABASE", newDatabase);
        editor.apply();
    }

    public void registerStatsListener (StatsListener statsListener) {
        this.statsListener = statsListener;
    }

    public void unRegisterStatsListener (StatsListener statsListener) {
        this.statsListener = null;
    }

    public void registerGamesListener (GamesListener gamesListener) {
        this.gamesListener = gamesListener;
    }

    public void unRegisterGamesListener (GamesListener gamesListener) {
        this.gamesListener = null;
    }

    public void registerNamesListener (NamesListener namesListener) {
        this.namesListener = namesListener;
        fetchNames();
    }

    public void unRegisterNamesListener() {
        for (String key : userRefs.keySet())
            Objects.requireNonNull(userRefs.get(key)).removeEventListener(namesListener(key));
        this.namesListener = null;
    }

    public void registerMetaGamesListener(MetaGamesListener metaGamesListener) {
        metaGamesListeners.add(metaGamesListener);
        fetchMetaGameData();
    }

    public void registerMetaPlayersListener(MetaPlayersListener metaPlayersListener) {
        this.metaPlayersListener = metaPlayersListener;
        fetchMetaPlayerData();
    }

    public void unRegisterMetaGamesListener(MetaGamesListener metaGamesListener) {
        metaGamesListeners.remove(metaGamesListener);
        if (metaGamesListeners.isEmpty()) {
            for (String key : userRefs.keySet())
                Objects.requireNonNull(userRefs.get(key)).removeEventListener(metaGamesListener(key));
        }
    }

    public void unregisterMetaPlayersListener() {
        for (String key : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(key)).removeEventListener(metaPlayersListener(key));
        }
        this.metaPlayersListener = null;
    }

    public String getShortId() {
        if (uid == null) {
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
                updateSharedPreferences(getShortId());
                initializeDatabase();
            }

        });
    }

    public void fetchName (String dbid, String pid, OnSuccessListener<String> onSuccessListener) {
        userRefs.get(dbid).child("names/" + pid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onSuccessListener.onSuccess(task.getResult().getValue(String.class));
            }
        });
    }

    public void fetchPlayersById (String dbid, String gid, OnSuccessListener<List<String>> onSuccessListener) {
        userRefs.get(dbid).child("games/players/" + gid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> players = (List<String>) task.getResult().getValue();
                onSuccessListener.onSuccess(players);
            }
        });
    }

    public void fetchTossesById (String dbid, String gid, OnSuccessListener<Map<String, Object>> onSuccessListener) {
        userRefs.get(dbid).child("tosses/" + gid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Map<String, Object> tosses = (Map<String, Object>) task.getResult().getValue();
                onSuccessListener.onSuccess(tosses);
            }
        });
    }

    public void fetchGamesAndWins (String pid) {
        for (String key : userRefs.keySet()) {
            userRefs.get(key).child("players/" + pid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            Map<String, Map<String, Map<String, Boolean>>> data = new HashMap<>();
                            Map<String, Map<String, Boolean>> allGames = new HashMap<>();
                            Map<String, Boolean> games = new HashMap<>();
                            for (DataSnapshot gameId : task.getResult().getChildren())
                                games.put(gameId.getKey(), gameId.getValue(Boolean.class));
                            allGames.put(pid, games);
                            data.put(key, allGames);
                            statsListener.onGamesReceived(data);
                        }
                    }
                }
            });
        }
    }

    public void fetchTosses (String dbid, String gid, String pid, OnSuccessListener<List<Long>> onSuccessListener) {
        userRefs.get(dbid).child("tosses/" + gid + "/" + pid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Long> tosses = (List<Long>) task.getResult().getValue();
                onSuccessListener.onSuccess(tosses);
            }
        });
    }


    public void fetchGamesById (String pid) {
        for (String key : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(key)).child("players/" + pid).get().addOnCompleteListener(idTask -> {
                if (idTask.isSuccessful()) {
                    for (DataSnapshot ds : idTask.getResult().getChildren()) {
                        userRefs.get(key).child("games/meta/" + ds.getKey()).get().addOnCompleteListener(gameTask -> {
                            MetaData metaData = gameTask.getResult().getValue(MetaData.class);
                            if (alterEgos.contains(Objects.requireNonNull(metaData).getWinner())) {
                                SavedGamesActivity.GameInfo gameInfo = new SavedGamesActivity.GameInfo(key, ds.getKey(), alterEgos.getString(metaData.getWinner(), null), metaData.getTimestamp());
                                gamesListener.onGameReceived(gameInfo);
                            }
                            else {
                                fetchName(key, metaData.getWinner(), name -> {
                                    SavedGamesActivity.GameInfo gameInfo = new SavedGamesActivity.GameInfo(key, ds.getKey(), name, metaData.getTimestamp());
                                    gamesListener.onGameReceived(gameInfo);
                                });
                            }
                        });
                    }
                }
            });
        }
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
        firebase.getReference("databases/" + getShortId() + "/created").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    addUser(getShortId(), null, null);
                else
                    Log.d("error", Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    public void addUser(String database, OnSuccessListener<Void> onSuccessListener, OnFailureListener error) {
        if (database.equals(""))
            return;
        if (!database.equals(dbid))
            updateSharedPreferences(database);
        databaseRef = firebase.getReference("databases").child(database);
        databaseRef.child("users").addChildEventListener(usersListener);
        databaseRef.child("users/" + uid).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful() && onSuccessListener != null) {
                onSuccessListener.onSuccess(null);
            }
            else if (error != null)
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
            homeRef.child("players").child(player.getId()).child(game.getId()).setValue(game.getPlayer(0).getId().equals(player.getId()));
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

    public void searchLiveGame (String gameId, OnSuccessListener<String> response, OnFailureListener error) {
        liveRef.child(gameId).child("started").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().getValue() == null) {
                    error.onFailure(new Exception("game not found"));
                    return;
                }
                response.onSuccess(gameId);
            }
            else
                error.onFailure(Objects.requireNonNull(task.getException()));
        });
    }

    public void fetchLiveGamePlayers (String gameId, OnSuccessListener<List<PlayerInfo>> response, OnFailureListener error) {
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
                error.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }


    public void addLiveGame (Game game) {
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
        String id = getLiveGameId();
        Map<String, Object> players = new HashMap<>();
        players.put("players", game.getPlayers());
        liveRef.child(id).updateChildren(players).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> timestamp = new HashMap<>();
                timestamp.put("started", ServerValue.TIMESTAMP);
                liveRef.child(id).updateChildren(timestamp);
            }
        });
        List<Toss> tosses = new ArrayList<>();
        for (int i = 0; i < tossesCount; i++) {
            Toss toss = new Toss(game.getPlayer(0).getId(), game.getPlayer(0).getUndoStack().peek());
            tosses.add(toss);
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

    public void postTosses (List<Toss> tosses) {
        liveRef.child(getLiveGameId() + "/tosses/").setValue(tosses).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> timestamp = new HashMap<>();
                timestamp.put("updated", ServerValue.TIMESTAMP);
                liveRef.child(getLiveGameId()).updateChildren(timestamp);
            }
        });
    }

    public void setLiveGameListener (String id, LiveGameListener liveGameListener) {
        this.liveGameListener = liveGameListener;
        liveRef.child(id).addValueEventListener(liveGameEventListener);
    }

    public void removeLiveGameListener (String id) {
        this.liveGameListener = null;
        liveRef.child(id).removeEventListener(liveGameEventListener);
    }


}

