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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class FirebaseManager {

    public static final int DATABASE_ID_LENGTH = 6;     // only even numbers allowed
    public static final int LIVEGAME_ID_LENGTH = 4;
    private boolean signingIn = false;
    private static FirebaseManager instance;
    private final FirebaseDatabase firebase = FirebaseDatabase.getInstance("https://molkky-8a33a-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference databaseRef;
    private final Map<String, DatabaseReference> userRefs = new HashMap<>();
    private final DatabaseReference liveRef = firebase.getReference("livegames");
    private StatsListener statsListener;
    private GamesListener gamesListener;
    private NamesListener namesListener;
    private final List<MetaGamesListener> metaGamesListeners = new ArrayList<>();
    private final List<SignInListener> signInListeners = new ArrayList<>();
    private MetaPlayersListener metaPlayersListener;
    private final List<LiveGameListener> liveGameListeners = new ArrayList<>();

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
            if (!metaGamesListeners.isEmpty())
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
            for (LiveGameListener liveGameListener : liveGameListeners)
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

    public interface SignInListener {
        void onSignInCompleted();
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
        void onGamesReceived(String dbid, PlayerStats player, DataSnapshot data);
        void onPlayersReceived(DataSnapshot data);
    }

    public interface LiveGameListener {
        void onLiveGameChange (GameHandler.LiveGame liveGame);
    }

    public static FirebaseManager getInstance(Context context) {
        if (instance == null)
            instance = new FirebaseManager(context.getApplicationContext());
        if (FirebaseAuth.getInstance().getUid() == null)
            FirebaseManager.instance.signIn();
        return instance;
    }

    private FirebaseManager (Context context) {
        preferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        alterEgos = context.getSharedPreferences("ALTER_EGOS", Context.MODE_PRIVATE);
        uid = FirebaseAuth.getInstance().getUid();
        dbid = preferences.getString("DATABASE", getShortId());
        addUser(dbid, null, null);
    }

    private void updateSharedPreferences(String newDatabase) {
        dbid = newDatabase;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("DATABASE", newDatabase);
        editor.apply();
    }

    public void registerImagesListener (ChildEventListener listener) {
        firebase.getReference("images/").addChildEventListener(listener);
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

    public void registerSignInListener(SignInListener signInListener) {
        signInListeners.add(signInListener);
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
        if (signingIn)
            return;
        signingIn = true;
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task -> {
            signingIn = false;
            if (task.isSuccessful()) {
                uid = FirebaseAuth.getInstance().getUid();
                updateSharedPreferences(getShortId());
                initializeDatabase(onSuccess -> {
                    for (SignInListener signInListener : signInListeners)
                        signInListener.onSignInCompleted();
                });
            }

        });
    }

    public void addImage (String pid, OnSuccessListener<String> onSuccessListener, OnFailureListener onFailureListener) {
        firebase.getReference("images/" + pid).setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onSuccessListener.onSuccess(pid);
            }
            else {
                onFailureListener.onFailure(Objects.requireNonNull(task.getException()));
            }
        });
    }

    public void fetchName (String dbid, String pid, OnSuccessListener<String> onSuccessListener) {
        Objects.requireNonNull(userRefs.get(dbid)).child("names/" + pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onSuccessListener.onSuccess(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void fetchPlayersById (String dbid, String gid, OnSuccessListener<List<String>> listener) {
        Objects.requireNonNull(userRefs.get(dbid)).child("games/players/" + gid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listener.onSuccess((List<String>) snapshot.getValue());
                    Objects.requireNonNull(userRefs.get(dbid)).child("games/players/" + gid).removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Objects.requireNonNull(userRefs.get(dbid)).child("games/players/" + gid).removeEventListener(this);

            }
        });
    }

    public void fetchTossesById (String dbid, String gid, OnSuccessListener<Map<String, List<Long>>> listener) {
        Objects.requireNonNull(userRefs.get(dbid)).child("tosses/" + gid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listener.onSuccess((Map<String, List<Long>>) snapshot.getValue());
                    Objects.requireNonNull(userRefs.get(dbid)).child("tosses/ + gid").removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void fetchGamesAndWins () {
        for (String key : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(key)).child("players/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    statsListener.onPlayersReceived(snapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void fetchGamesAndWins (PlayerStats player) {
        for (String dbid : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(dbid)).child("players/" + player.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (statsListener != null)
                            statsListener.onGamesReceived(dbid, player, snapshot);
                        Objects.requireNonNull(userRefs.get(dbid)).child("players/" + player.getId()).removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Objects.requireNonNull(userRefs.get(dbid)).child("players/" + player.getId()).removeEventListener(this);
                }
            });
        }
    }

    public void fetchTosses (String dbid, String gid, String pid, OnSuccessListener<List<Long>> onSuccessListener) {
        Objects.requireNonNull(userRefs.get(dbid)).child("tosses/" + gid + "/" + pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Long> tosses = (List<Long>) snapshot.getValue();
                onSuccessListener.onSuccess(tosses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void fetchTosses (OnSuccessListener<Set<DataSnapshot>> onSuccessListener) {
        Set<String> databases = userRefs.keySet();
        Set<String> fetchedDatabases = new HashSet<>();
        Set<DataSnapshot> result = new HashSet<>();
        for (String key : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(key)).child("tosses/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    fetchedDatabases.add(key);
                    result.add(snapshot);
                    if (fetchedDatabases.equals(databases))
                        onSuccessListener.onSuccess(result);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    public void fetchGamesById (String pid) {
        for (String key : userRefs.keySet()) {
            Objects.requireNonNull(userRefs.get(key)).child("players/" + pid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Objects.requireNonNull(userRefs.get(key)).child("games/meta/" + ds.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                MetaData metaData = snapshot.getValue(MetaData.class);
                                if (alterEgos.contains(Objects.requireNonNull(metaData).getWinner())) {
                                    SavedGamesActivity.GameInfo gameInfo = new SavedGamesActivity.GameInfo(key, ds.getKey(), alterEgos.getString(metaData.getWinner(), null), metaData.getTimestamp());
                                    gamesListener.onGameReceived(gameInfo);
                                } else {
                                    fetchName(key, metaData.getWinner(), name -> {
                                        SavedGamesActivity.GameInfo gameInfo = new SavedGamesActivity.GameInfo(key, ds.getKey(), name, metaData.getTimestamp());
                                        gamesListener.onGameReceived(gameInfo);
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void fetchCreated(OnSuccessListener<Long> response, OnFailureListener failureListener) {
        if (databaseRef == null) {
            failureListener.onFailure(new Exception("Database not initialized"));
            return;
        }
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

    private void initializeDatabase(OnSuccessListener<Void> listener) {
        firebase.getReference("databases/" + getShortId() + "/created").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                addUser(getShortId(), null, null);
                if (listener != null)
                    listener.onSuccess(null);
            }

            else
                Log.d("error", Objects.requireNonNull(task.getException()).getMessage());
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
        Map<String, Object> data = new HashMap<>();
        data.put("players", game.getPlayers());
        List<Toss> tosses = new ArrayList<>();
        for (int i = 0; i < tossesCount; i++) {
            Toss toss = new Toss(game.getPlayer(0).getId(), game.getPlayer(0).getUndoStack().peek());
            tosses.add(toss);
            game.addToss(game.getPlayer(0).getUndoStack().pop());
        }
        if (tosses.size() > 0)
            data.put("tosses", tosses);
        data.put("started", ServerValue.TIMESTAMP);
        data.put("updated", ServerValue.TIMESTAMP);
        liveRef.child(id).setValue(data);
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

    public void addLiveGameListener(String id, LiveGameListener liveGameListener) {
        liveGameListeners.add(liveGameListener);
        liveRef.child(id).addValueEventListener(liveGameEventListener);
    }

    public void removeLiveGameListener (String id, LiveGameListener liveGameListener) {
        if (id == null)
            return;
        liveGameListeners.remove(liveGameListener);
        if (liveGameListeners.isEmpty())
            liveRef.child(id).removeEventListener(liveGameEventListener);
    }

}

