package com.teskola.molkky;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Game {
    private final ArrayList<Player> players;
    private String id;

    public Game(String gameId, ArrayList<Player> players) {
        this.players = players;
        this.id = gameId;
    }

    public Game(ArrayList<Player> players, int turn, boolean random) {
        this.id = UUID.randomUUID().toString().substring(0,8);
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
    public String getId() {
        return id;
    }

}
