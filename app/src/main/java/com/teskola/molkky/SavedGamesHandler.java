package com.teskola.molkky;

import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
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
        Map<String, Object> allTosses = new HashMap<>();
        List<Player> players = new ArrayList<>();
        firebaseManager.fetchTossesById(dbid, gid, tossesMap -> {
            allTosses.putAll(tossesMap);
            firebaseManager.fetchPlayersById(dbid, gid, pids -> {
                for (String pid : pids) {
                    String name = PlayerHandler.getInstance(context).getPlayerName(dbid, pid);
                    List<Long> tosses = (List<Long>) tossesMap.get(pid);
                    players.add(new Player(pid, name, tosses));
                }
                Game game = new Game(players);
                onSuccessListener.onSuccess(game);
            });
        });
    }

    @Override
    public void onGamesReceived(Map<String, Set<FirebaseManager.MetaData>> data) {
        for (String key : data.keySet()) {
            for (FirebaseManager.MetaData metaData : Objects.requireNonNull(data.get(key))) {
                String name = PlayerHandler.getInstance(context).getPlayerName(key, metaData.getWinner());
                SavedGamesActivity.GameInfo gameInfo = new SavedGamesActivity.GameInfo(key, metaData.getId(), name, metaData.getTimestamp());
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
