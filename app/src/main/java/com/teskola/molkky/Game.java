package com.teskola.molkky;


import com.google.firebase.database.Exclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private final List<Player> players;
    private String id;

    public Game(List<Player> players) {
        this.players = players;
    }

    public Game() {
        players = new ArrayList<>();
    }

    public Game(List<Player> players, boolean random) {
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
        }

    }

    @Exclude
    public int getTossesCount () {
        int count = 0;
        for (Player player : players)
            count += player.getTosses().size();
        return count;
    }

    public void setId (String id) {
        this.id = id;
    }

    public List<Player> getPlayers() {
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

    public void addToss (long points) {
        players.get(0).addToss(points);
        if (getPlayer(0).countAll() != 50) {
            setTurn(1);
            while (players.get(0).isEliminated())
                setTurn(1);
        }
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

}
