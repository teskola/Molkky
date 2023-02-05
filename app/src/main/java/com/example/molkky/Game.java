package com.example.molkky;

import java.util.ArrayList;
import java.util.Random;

public class Game {
    private final ArrayList<Player> players;
    private int id;

    public Game(ArrayList<Player> players, int turn, boolean random) {
        this.id = -1;
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
    public void clear() {
        for (Player player : players) {
            player.clearTosses();
            player.clearUndoStack();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
