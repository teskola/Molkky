package com.teskola.molkky;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Game implements Comparable<Game> {
    private ArrayList<Player> players;
    @Exclude
    private String id;
    private long timestamp;

    public Game(String gameId, ArrayList<Player> players) {
        this.players = players;
        this.id = gameId;
    }

    public Game() {}

    public Game(ArrayList<Player> players, int turn, boolean random) {
        if (random) {
            ArrayList<Player> randomized = new ArrayList<>();
            while (!players.isEmpty()) {
                Random rand = new Random();
                int index = rand.nextInt(players.size());
                randomized.add(players.get(index));
                players.remove(index);
            }
            this.players = randomized;
        }
        else {
            this.players = players;
            setTurn(turn);
        }
    }


    public void setId (String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Player getPlayer(int position) {
        return players.get(position);
    }

    public void setTurn (int index) {
        ArrayList<Player> transferred = new ArrayList<>();
        for (int i=0; i < index ; i++) {
            transferred.add(players.get(i));
        }
        if (index > 0) {
            players.subList(0, index).clear();
        }
        players.addAll(transferred);
    }

    public boolean allDropped() {
        for (int i=1; i < players.size(); i++) {
            if (!players.get(i).isEliminated()) {
                return false;
            }
        }
        return true;
    }
    @Exclude
    public String getId() {
        return id;
    }

    @Override
    public int compareTo(Game game) {
        return Long.compare(game.timestamp, this.timestamp);
    }
}
