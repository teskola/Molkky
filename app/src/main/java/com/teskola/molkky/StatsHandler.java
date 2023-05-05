package com.teskola.molkky;

import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsHandler implements FirebaseManager.StatsListener {
    private final FirebaseManager firebaseManager;
    private final List<PlayerStats> playerStats;
    private final DataChangedListener dataChangedListener;

    public StatsHandler (Context context, List<PlayerStats> playerStats, DataChangedListener dataChangedListener) {
        this.playerStats = playerStats;
        this.dataChangedListener = dataChangedListener;
        this.firebaseManager = FirebaseManager.getInstance(context);
        firebaseManager.registerStatsListener(this);

    }

    public void close() {
        firebaseManager.unRegisterStatsListener(this);
    }

    public interface DataChangedListener {
        void onDataChanged();
    }

    public void getPlayerStats (PlayerInfo playerInfo) {
        firebaseManager.fetchGamesAndWins(playerInfo.getId());
    }

    @Override
    public void onGamesReceived(Map<String, Map<String, Map<String, Boolean>>> data) {
        for (String dbid : data.keySet()) {
            for (String pid : data.get(dbid).keySet()) {
                for (Map.Entry<String, Boolean> game : data.get(dbid).get(pid).entrySet()) {
                    firebaseManager.fetchTosses(dbid, game.getKey(), pid, new OnSuccessListener<List<Long>>() {
                        @Override
                        public void onSuccess(List<Long> tosses) {
                            for (PlayerStats player : playerStats) {
                                if (player.getId().equals(pid)) {
                                    Map<String, List<Long>> tossesMap = new HashMap<>();
                                    tossesMap.put(game.getKey(), tosses);
                                    player.addTosses(tossesMap);
                                    if (game.getValue().equals(Boolean.TRUE))
                                        player.addWin();
                                    dataChangedListener.onDataChanged();
                                    break;
                                }
                            }

                        }
                    });
                }
            }
        }
    }
}
