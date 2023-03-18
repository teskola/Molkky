package com.teskola.molkky;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Database {

    private HashMap<String, HashMap<String, Game>> databaseMap = new HashMap<>();
    private HashMap<String, PlayerInfo> playersMap = new HashMap<>();

    public Database () {}

    public Database (HashMap<String, HashMap<String, Game>> databaseMap, HashMap<String, PlayerInfo> playersMap) {
        this.databaseMap = databaseMap;
        this.playersMap = playersMap;
    }

    public void addGames(String key, HashMap<String, Game> gamesMap) {
        databaseMap.put(key, gamesMap);
        // TODO check for duplicate names
    }

    public void removeDatabase(String key) {
        databaseMap.remove(key);
        // TODO tarkista playersmap games=0
    }

    public void addGame(String key, String gameId, Game game) {
        if (databaseMap.containsKey(key))
            databaseMap.get(key).put(gameId, game);
        else {
            HashMap<String,Game> newMap = new HashMap<>();
            newMap.put(gameId, game);
            databaseMap.put(key, newMap);
        }
    }

    public void removeGame(String key, String gameId) {
        databaseMap.get(key).remove(gameId);
    }

    public void addPlayer(String id, PlayerInfo player) {
        playersMap.put(id, player);
    }

    public void removePlayer(String id) {
        // TODO if (games==0) remove
    }

    public String getPlayerId(String name) {
        for (Map.Entry<String, PlayerInfo> entry : playersMap.entrySet()) {
            if (Objects.equals(name, entry.getValue().getName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public List<Game> getGames () {
        List<Game> allGames = new ArrayList<>();
        for (Map.Entry<String, HashMap<String , Game>> entry : databaseMap.entrySet()) {
            HashMap<String, Game> gamesMap;
            gamesMap = entry.getValue();
            Collection<Game> gamesCol = gamesMap.values();
            allGames.addAll(gamesCol);
        }
        Collections.sort(allGames);
        return allGames;
    }


    }
