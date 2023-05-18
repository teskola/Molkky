package com.teskola.molkky;

import android.content.Context;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class StatsHandler implements FirebaseManager.StatsListener {
    private boolean tossesFetched = false;
    private final FirebaseManager firebaseManager;
    private final List<PlayerStats> playerStats;
    private final StatsReceivedListener statsReceivedListener;

    public StatsHandler (Context context, List<PlayerStats> playerStats, StatsReceivedListener statsReceivedListener) {
        this.playerStats = playerStats;
        this.statsReceivedListener = statsReceivedListener;
        this.firebaseManager = FirebaseManager.getInstance(context);
        firebaseManager.registerStatsListener(this);
    }

    public StatsHandler (Context context, StatsReceivedListener statsReceivedListener) {
        this.firebaseManager = FirebaseManager.getInstance(context);
        this.statsReceivedListener = statsReceivedListener;
        List<PlayerInfo> players = PlayerHandler.getInstance(context).getPlayers();
        playerStats = new ArrayList<>(players.size());
        for (PlayerInfo player : players) {
            playerStats.add(new PlayerStats(player, null, null));
        }
        firebaseManager.registerStatsListener(this);
        firebaseManager.fetchGamesAndWins();
    }


    public StatsHandler (Context context, PlayerInfo[] playerInfos, StatsReceivedListener statsReceivedListener) {
        this.firebaseManager = FirebaseManager.getInstance(context);
        this.statsReceivedListener = statsReceivedListener;
        playerStats = new ArrayList<>(playerInfos.length);
        for (PlayerInfo player : playerInfos) {
            playerStats.add(new PlayerStats(player, null, null));
        }
        firebaseManager.registerStatsListener(this);
    }

    public List<PlayerStats> getPlayers() {
        return playerStats;
    }

    public void close() {
        firebaseManager.unRegisterStatsListener(this);
    }

    public interface StatsReceivedListener {
        void onDataReceived();
    }

    public void getAllStats () {
        if (tossesFetched) {
            statsReceivedListener.onDataReceived();
            return;
        }
        firebaseManager.fetchTosses(data -> {
            for (DataSnapshot ds : data) {
                if (!ds.exists())
                    break;
                for (DataSnapshot gameDS : ds.getChildren()) {
                    for (DataSnapshot playerDS : gameDS.getChildren()) {
                        int index = 0;
                        while (!playerStats.get(index).getId().equals(playerDS.getKey()))
                            index++;
                        List<Long> tosses = (List<Long>) playerDS.getValue();
                        Map<String, List<Long>> tossesMap = new HashMap<>(1);
                        tossesMap.put(gameDS.getKey(), tosses);
                        playerStats.get(index).addTosses(tossesMap);
                    }
                }
            }
            tossesFetched = true;
            statsReceivedListener.onDataReceived();
        });
    }

    public void getPlayerStats (int position) {
        firebaseManager.fetchGamesAndWins(playerStats.get(position));
    }

    @Override
    public void onGamesReceived(String dbid, PlayerStats player, DataSnapshot data) {

        Set<String> games = new HashSet<>((int) data.getChildrenCount());
        Set<String> gamesReceived = new HashSet<>((int) data.getChildrenCount());
        for (DataSnapshot ds : data.getChildren()) {
            games.add(ds.getKey());
            if (Objects.equals(ds.getValue(Boolean.class), Boolean.TRUE))
                player.addWin(ds.getKey());
        }

        for (String gid : games) {
            firebaseManager.fetchTosses(dbid, gid, player.getId(), tosses -> {
                gamesReceived.add(gid);
                Map<String, List<Long>> tossesMap = new HashMap<>(1);
                tossesMap.put(gid, tosses);
                player.addTosses(tossesMap);
                if (gamesReceived.equals(games))
                    statsReceivedListener.onDataReceived();
            });
        }
    }

    @Override
    public void onPlayersReceived(DataSnapshot data) {
        for (DataSnapshot playerDS : data.getChildren()) {
            int index = 0;
            while (!playerStats.get(index).getId().equals(playerDS.getKey()))
                index++;            
            for (DataSnapshot gameDS : playerDS.getChildren()) {
                Map<String, List<Long>> tossesMap = new HashMap<>(1);
                tossesMap.put(gameDS.getKey(), null);
                playerStats.get(index).addTosses(tossesMap);
                if (Objects.equals(gameDS.getValue(Boolean.class), Boolean.TRUE))
                    playerStats.get(index).addWin(gameDS.getKey());
            }
        }
        statsReceivedListener.onDataReceived();
    }
}
