package com.teskola.molkky;

import android.content.Context;
import android.util.Log;

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
        firebaseManager.fetchPlayersById(dbid, gid, new OnSuccessListener<List<String>>() {
            @Override
            public void onSuccess(List<String> pids) {
                firebaseManager.fetchTossesById(dbid, gid, tosses -> {
                    Player[] players = new Player[pids.size()];
                    for (String pid : tosses.keySet()) {
                        PlayerInfo player = PlayerHandler.getInstance(context).getPlayer(dbid, pid);
                        List<Long> tossesList = tosses.get(pid);
                        int index = pids.indexOf(pid);
                        players[index] = new Player(player, tossesList);
                    }
                    Game game = new Game(Arrays.asList(players));
                    onSuccessListener.onSuccess(game);
                });
            }
        });
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
