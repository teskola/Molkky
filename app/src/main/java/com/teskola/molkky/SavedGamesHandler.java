package com.teskola.molkky;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SavedGamesHandler implements FirebaseManager.MetaGamesListener {
    private final List<SavedGamesActivity.GameInfo> games;
    private final GamesChangedListener gamesChangedListener;
    private final Context context;
    private final FirebaseManager firebaseManager;

    private ValueEventListener tossesListener(String dbid, List<String> pids, OnSuccessListener<Game> response) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Player[] players = new Player[pids.size()];
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = PlayerHandler.getInstance(context).getPlayerName(dbid, ds.getKey());
                    List<Long> tosses = (List<Long>) ds.getValue();
                    int index = pids.indexOf(ds.getKey());
                    players[index] = new Player(ds.getKey(), name, tosses);

                }
                Game game = new Game(Arrays.asList(players));
                response.onSuccess(game);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private ValueEventListener playersListener(OnSuccessListener<List<String>> response) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                response.onSuccess((List<String>) snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }


    public SavedGamesHandler (Context context, List<SavedGamesActivity.GameInfo> games, GamesChangedListener gamesChangedListener) {
        this.context = context;
        this.gamesChangedListener = gamesChangedListener;
        this.games = games;
        this.firebaseManager = FirebaseManager.getInstance(context);
        if (gamesChangedListener != null)
            firebaseManager.registerMetaGamesListener(this);
    }

    public void close() {
        firebaseManager.unRegisterMetaGamesListener(this);
    }

    public interface GamesChangedListener {
        void onGamesChanged();
    }


    public void getGame(String dbid, String gid, OnSuccessListener<Game> onSuccessListener) {
        firebaseManager.fetchPlayersById(dbid, gid,
                playersListener(pids -> firebaseManager.fetchTossesById(dbid, gid,
                        tossesListener(dbid, pids, onSuccessListener))));
    }

    @Override
    public void onGamesReceived(Map<String, Set<FirebaseManager.MetaData>> data) {
        for (String key : data.keySet()) {
            for (FirebaseManager.MetaData metaData : Objects.requireNonNull(data.get(key))) {
                String name = PlayerHandler.getInstance(context).getPlayerName(key, metaData.getWinner());
                SavedGamesActivity.GameInfo gameInfo = new SavedGamesActivity.GameInfo(key, metaData.getId(), name, metaData.getTimestamp());
                if (!games.contains(gameInfo))
                    games.add(gameInfo);
            }
        }
        Collections.sort(games);
        gamesChangedListener.onGamesChanged();
    }

    @Override
    public void onDatabaseRemoved(String id) {
        boolean gamesRemoved = false;
        for (SavedGamesActivity.GameInfo game : games) {
            if (game.getDbid().equals(id)) {
                games.remove(game);
                gamesRemoved = true;
            }
        }
        if (gamesRemoved)
            gamesChangedListener.onGamesChanged();
    }
}
