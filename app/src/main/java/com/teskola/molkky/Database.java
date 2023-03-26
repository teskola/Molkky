package com.teskola.molkky;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Database {

    private HashMap<String, HashMap<String, Game>> databaseMap = new HashMap<>();
    private HashMap<String, PlayerInfo> playersMap = new HashMap<>();
    long created = 0;

    public Database() {
    }

    public Database(HashMap<String, HashMap<String, Game>> databaseMap, HashMap<String, PlayerInfo> playersMap) {
        this.databaseMap = databaseMap;
        this.playersMap = playersMap;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void removeDatabase(String key) {
        Set<String> delete = databaseMap.keySet();
        delete.remove(key);
        databaseMap.remove(delete);

        // Remove players, who have zero games played

        ArrayList<String> ids = new ArrayList<>();
        for (String id : playersMap.keySet())
            ids.add(id);
        for (String id : ids)
            removePlayer(id);
    }

    private void addPlayers(Game game) {
        ArrayList<Player> players = game.getPlayers();
        for (Player player : players) {
            if (!playersMap.containsKey(player.getId())) {
                int i = 1;
                while (nameExists(player.getName())) {
                    player.setName(player.getName() + i);
                }
                playersMap.put(player.getId(), player);
            }
        }
    }

    public void addGame(String key, String gameId, Game game) {
        addPlayers(game);
        if (databaseMap.containsKey(key))
            databaseMap.get(key).put(gameId, game);
        else {
            HashMap<String, Game> newMap = new HashMap<>();
            newMap.put(gameId, game);
            databaseMap.put(key, newMap);
        }
    }

    public void removeGame(String key, String gameId) {
        Game game = databaseMap.get(key).get(gameId);
        databaseMap.get(key).remove(gameId);
        ArrayList<Player> players = game.getPlayers();
        for (Player player : players)
            removePlayer(player.getId());
    }

    /*
     *
     *
     * Removes player if no games in database for this player.
     *
     * */

    public void removePlayer(String id) {
        if (getGames(id).isEmpty())
            playersMap.remove(id);
    }

    public String getPlayerId(String name) {
        for (Map.Entry<String, PlayerInfo> entry : playersMap.entrySet()) {
            if (Objects.equals(name, entry.getValue().getName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public List<PlayerInfo> getPlayers() {
        ArrayList<PlayerInfo> players = new ArrayList<>();
        for (String id : playersMap.keySet()) {
            PlayerInfo player = playersMap.get(id);
            player.setId(id);
            players.add(player);
        }
        return players;
    }

    public int getPlayersCount() {
        return playersMap.size();
    }

    public int getGamesCount() {
        int count = 0;
        for (Map.Entry<String, HashMap<String, Game>> entry : databaseMap.entrySet()) {
            HashMap<String, Game> gamesMap;
            gamesMap = entry.getValue();
            count += gamesMap.keySet().size();
        }
        return count;
    }

    public int getTossesCount() {
        int count = 0;
        for (Map.Entry<String, HashMap<String, Game>> entry : databaseMap.entrySet()) {
            HashMap<String, Game> gamesMap;
            gamesMap = entry.getValue();
            for (String id : gamesMap.keySet()) {
                Game game = gamesMap.get(id);
                for (Player player : game.getPlayers()) {
                    count += player.getTosses().size();
                }
            }
        }
        return count;
    }

    public long lastUpdated() {
        long timestamp = created;
        for (Map.Entry<String, HashMap<String, Game>> entry : databaseMap.entrySet()) {
            HashMap<String, Game> gamesMap;
            gamesMap = entry.getValue();
            for (String id : gamesMap.keySet()) {
                if (gamesMap.get(id).getTimestamp() > timestamp)
                    timestamp = gamesMap.get(id).getTimestamp();
            }
        }
        return timestamp;
    }


    public String getPlayerName(String playerId) {
        return playersMap.get(playerId).getName();
    }

    // Overload this

    public PlayerStats getStats(PlayerInfo playerInfo) {
        String playerId = playerInfo.getId();
        HashSet<String> gameIds = (HashSet<String>) getGameIds(playerId);
        HashMap<String, ArrayList<Integer>> tosses = new HashMap<>();
        int wins = 0;

        for (Map.Entry<String, HashMap<String, Game>> entry : databaseMap.entrySet()) {
            HashMap<String, Game> gamesMap;
            gamesMap = entry.getValue();
            for (String key : gameIds) {
                Game game = gamesMap.get(key);
                if (game != null && game.getPlayers().get(0).getId().equals(playerId))
                    wins++;
                if (game != null) {
                    for (Player player : game.getPlayers()) {
                        if (player.getId().equals(playerId)) {
                            tosses.put(key, player.getTosses());
                        }
                    }
                }

            }
        }
        return new PlayerStats(playerInfo, wins, tosses);
    }

    public Collection<String> getGameIds(String playerId) {
        HashSet<String> ids = new HashSet<>();
        for (Map.Entry<String, HashMap<String, Game>> entry : databaseMap.entrySet()) {
            HashMap<String, Game> gamesMap;
            gamesMap = entry.getValue();
            for (String key : gamesMap.keySet()) {
                Game game = gamesMap.get(key);
                ArrayList<Player> players = game.getPlayers();
                for (Player player : players) {
                    if (player.getId().equals(playerId)) {
                        ids.add(key);
                        break;
                    }
                }
            }
        }
        return ids;
    }

    public List<PlayerInfo> getPlayers(ArrayList<PlayerInfo> excludedPlayers) {
        ArrayList<String> excludedIds = new ArrayList<>();
        for (PlayerInfo playerInfo : excludedPlayers)
            excludedIds.add(playerInfo.getId());
        ArrayList<PlayerInfo> players = new ArrayList<>();
        for (String id : playersMap.keySet()) {
            PlayerInfo player = playersMap.get(id);
            player.setId(id);
            if (!excludedIds.contains(id))
                players.add(player);
        }
        return players;
    }


    public List<Game> getGames() {
        List<Game> allGames = new ArrayList<>();
        for (Map.Entry<String, HashMap<String, Game>> entry : databaseMap.entrySet()) {
            HashMap<String, Game> gamesMap;
            gamesMap = entry.getValue();
            for (String id : gamesMap.keySet()) {
                Game game = gamesMap.get(id);
                game.setId(id);
                allGames.add(game);
            }
        }
        Collections.sort(allGames);
        return allGames;
    }

    public List<Game> getGames(String playerId) {
        List<Game> allGames = new ArrayList<>();
        for (Map.Entry<String, HashMap<String, Game>> entry : databaseMap.entrySet()) {
            HashMap<String, Game> gamesMap;
            gamesMap = entry.getValue();
            for (String id : gamesMap.keySet()) {
                Game game = gamesMap.get(id);
                game.setId(id);
                ArrayList<Player> players = game.getPlayers();
                for (Player player : players) {
                    if (player.getId().equals(playerId)) {
                        allGames.add(game);
                        break;
                    }
                }
            }
        }
        Collections.sort(allGames);
        return allGames;
    }


    public boolean nameExists(String name) {
        for (PlayerInfo playerInfo : playersMap.values()) {
            if (name.equals(playerInfo.getName()))
                return true;
        }
        return false;
    }

    public boolean noPlayers() {
        return playersMap.isEmpty();
    }

    public Game getGame(String gameId) {
        for (Map.Entry<String, HashMap<String, Game>> entry : databaseMap.entrySet()) {
            Game result = entry.getValue().get(gameId);
            if (result != null) return result;
        }
        return null;
    }


}
