package com.teskola.molkky;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlayerHandler implements FirebaseManager.NamesListener {

    private final Map<String, Set<PlayerInfo>> playersMap;
    private final SharedPreferences alterEgos;
    private static PlayerHandler instance;

    public static PlayerHandler getInstance(Context context) {
        if (instance == null)
            instance = new PlayerHandler(context.getApplicationContext());
        return instance;
    }

    private PlayerHandler (Context context) {
        playersMap = new HashMap<>();
        alterEgos = context.getSharedPreferences("ALTER_EGOS", Context.MODE_PRIVATE);
        FirebaseManager.getInstance(context).registerNamesListener(this);
    }

    @Override
    public void onPlayersReceived(Map<String, Set<PlayerInfo>> players) {
        playersMap.putAll(players);
    }

    @Override
    public void onDatabaseRemoved(String key) {
        playersMap.remove(key);
    }

    public void clear() {
        playersMap.clear();
    }

    public boolean nameInDatabase (String name) {
        for (String db : playersMap.keySet()) {
            for (PlayerInfo player : Objects.requireNonNull(playersMap.get(db))) {
                if (player.getName().equals(name))
                    return true;
            }
        }
        return false;
    }

    public void changeName (PlayerInfo player, String newName) {
        String alterEgoId = findAlterEgoId(newName);
        SharedPreferences.Editor editor = alterEgos.edit();

        if (alterEgoId != null) {
            editor.remove(alterEgoId);
            editor.apply();
        }
        PlayerInfo namesMapPlayer = findPlayer(findPlayerId(player.getName()));
        player.setAlterEgo(newName);
        if (namesMapPlayer != null)
            namesMapPlayer.setAlterEgo(newName);
        editor.putString(player.getId(), newName);
        editor.apply();

    }

    public boolean addPlayer(PlayerInfo player) {
        String alterEgoId = findAlterEgoId(player.getName());
        if (alterEgoId != null) {
            String databaseName = findPlayerName(alterEgoId);
            if (databaseName == null) {
                SharedPreferences.Editor editor = alterEgos.edit();
                editor.remove(alterEgoId);
                editor.apply();
                player.setId(UUID.randomUUID().toString().substring(0, 8));
                return false;
            }
            else {
                player.setAlterEgo(player.getName());
                player.setName(databaseName);
                player.setId(alterEgoId);
                return true;
            }

        }
        String playerId = findPlayerId(player.getName());
        if (playerId != null) {
            player.setId(playerId);
            return true;
        } else {
            player.setId(UUID.randomUUID().toString().substring(0, 8));
            return false;
        }
    }

    public boolean noSavedPlayers() {
        for (String db : playersMap.keySet()) {
            for (PlayerInfo player : Objects.requireNonNull(playersMap.get(db))) {
                if (player != null)
                    return false;
            }
        }
        return true;
    }

    private void addAlterEgos (Set<PlayerInfo> players) {
        Map<String, String> alterMap;
        alterMap = (Map<String, String>) alterEgos.getAll();
        if (!alterMap.isEmpty()) {
            for (PlayerInfo player : players) {
                if (alterMap.containsKey(player.getId())) {
                    player.setAlterEgo(alterMap.get(player.getId()));
                }
            }
        }
    }

    public List<PlayerInfo> getPlayers() {
        String uid = FirebaseAuth.getInstance().getUid();
        Set<PlayerInfo> homePlayers = new HashSet<>();
        Set<PlayerInfo> foreignPlayers = new HashSet<>();

        // Add players from own database

        if (playersMap.containsKey(uid)) {
            homePlayers.addAll(Objects.requireNonNull(playersMap.get(uid)));
        }
        addAlterEgos(homePlayers);

        // Select players from foreign databases. This removes duplicate ids.

        List<PlayerInfo> allPlayers = new ArrayList<>(homePlayers);
        for (String dbid : playersMap.keySet()) {
            if  (!dbid.equals(uid)) {
                for (PlayerInfo player : Objects.requireNonNull(playersMap.get(dbid))) {
                    boolean inHomePlayers = false;
                    for (PlayerInfo homePlayer : homePlayers) {
                        if (homePlayer.equals(player)) {
                            inHomePlayers = true;
                            break;
                        }
                    }
                    if (!inHomePlayers)
                        foreignPlayers.add(player);
                }
            }
        }
        addAlterEgos(foreignPlayers);

        // Handle duplicate names

        Set<String> namesSet = new HashSet<>();
        for (PlayerInfo player : homePlayers)
            namesSet.add(player.getName());

        for (PlayerInfo foreignPlayer : foreignPlayers) {

            int i = 1;
            while (!namesSet.add(foreignPlayer.getName())) {
                foreignPlayer.setAlterEgo(foreignPlayer.getName() + i);
                SharedPreferences.Editor editor = alterEgos.edit();
                editor.putString(foreignPlayer.getId(), foreignPlayer.getName());
                editor.apply();
            }
            allPlayers.add(foreignPlayer);
        }
        return allPlayers;
    }

    public List<PlayerInfo> getPlayers(List<PlayerInfo> excludedPlayers) {
        String uid = FirebaseAuth.getInstance().getUid();
        Set<PlayerInfo> homePlayers = new HashSet<>();
        Set<PlayerInfo> foreignPlayers = new HashSet<>();
        if (playersMap.containsKey(uid)) {
            for (PlayerInfo player : Objects.requireNonNull(playersMap.get(uid))) {
                if (!excludedPlayers.contains(player))
                    homePlayers.add(player);
            }
        }
        addAlterEgos(homePlayers);
        List<PlayerInfo> allPlayers = new ArrayList<>(homePlayers);
        for (String dbid : playersMap.keySet()) {
            if  (!dbid.equals(uid)) {
                for (PlayerInfo player : Objects.requireNonNull(playersMap.get(dbid))) {
                    boolean inHomePlayers = false;
                    for (PlayerInfo homePlayer : homePlayers) {
                        if (homePlayer.equals(player)) {
                            inHomePlayers = true;
                            break;
                        }
                    }
                    if (!excludedPlayers.contains(player) && !inHomePlayers)
                        foreignPlayers.add(player);
                }
            }
        }
        addAlterEgos(foreignPlayers);

        Set<String> namesSet = new HashSet<>();
        for (PlayerInfo player : homePlayers)
            namesSet.add(player.getName());

        for (PlayerInfo foreignPlayer : foreignPlayers) {

            int i = 1;
            while (!namesSet.add(foreignPlayer.getName())) {
               foreignPlayer.setAlterEgo(foreignPlayer.getName() + i);
               SharedPreferences.Editor editor = alterEgos.edit();
               editor.putString(foreignPlayer.getId(), foreignPlayer.getName());
               editor.apply();
            }
            allPlayers.add(foreignPlayer);
        }
        //noinspection ComparatorCombinators
        Collections.sort(allPlayers, (playerInfo1, playerInfo2) -> playerInfo1.getName().compareTo(playerInfo2.getName()));
        return allPlayers;
    }

    public PlayerInfo getPlayer (String dbid, String pid) {
        for (PlayerInfo playerInfo : Objects.requireNonNull(playersMap.get(dbid))) {
            if (playerInfo.getId().equals(pid)) {
                String alterEgo = alterEgos.getString(pid, null);
                if (alterEgo != null)
                    playerInfo.setAlterEgo(alterEgo);
                return playerInfo;
            }
        }
        return null;
    }


    public String getPlayerName (String dbid, String id) {
        String alterEgo = alterEgos.getString(id, null);
        if (alterEgo != null)
            return alterEgo;
        for (PlayerInfo playerInfo : Objects.requireNonNull(playersMap.get(dbid))) {
            if (playerInfo.getId().equals(id))
                return  playerInfo.getNameInDatabase();
        }
        return null;
    }

    public String getPlayerName (String id) {
        String alterEgo = alterEgos.getString(id, null);
        if (alterEgo != null)
            return alterEgo;
        else
            return findPlayerName(id);
    }

    private PlayerInfo findPlayer (String id) {
        for (String dbid : playersMap.keySet()) {
            for (PlayerInfo playerInfo : Objects.requireNonNull(playersMap.get(dbid))) {
                if (playerInfo.getId().equals(id))
                    return playerInfo;
            }
        }
        return null;
    }

    private String findPlayerName (String id) {
        for (String dbid : playersMap.keySet()) {
            for (PlayerInfo playerInfo : Objects.requireNonNull(playersMap.get(dbid))) {
                if (playerInfo.getId().equals(id))
                    return playerInfo.getName();
            }
        }
        return null;
    }

    private String findAlterEgoId (String name) {
        Map<String, String> alterEgosMap = (Map<String, String>) alterEgos.getAll();
        for (Map.Entry<String, String> alterEgo : alterEgosMap.entrySet()) {
            if (alterEgo.getValue().equals(name))
                return alterEgo.getKey();
        }
        return null;
    }

    private String findPlayerId (String name) {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null)
            return null;
        if (playersMap.containsKey(uid)) {
            for (PlayerInfo player : Objects.requireNonNull(playersMap.get(uid))) {
                if (player.getName().equals(name))
                    return player.getId();
            }
        }
        for (String dbid : playersMap.keySet()) {
            if (!dbid.equals(uid)) {
                for (PlayerInfo player : Objects.requireNonNull(playersMap.get(dbid))) {
                    if (player.getName().equals(name))
                        return player.getId();
                }
            }
        }
        return null;
    }

}
