package com.example.molkky;

import java.util.ArrayList;
import java.util.Random;

public class Game {
    private ArrayList<Player> players;

    public Game(ArrayList<String> players, int turn, boolean random) {
        this.players = new ArrayList<Player>();
        if (random) {
            while (!players.isEmpty()) {
                Random rand = new Random();
                int index = rand.nextInt(players.size());
                this.players.add(new Player(players.get(index)));
                players.remove(index);
            }
        }
        else {
            for (String i : players) {
                this.players.add(new Player(i));
            }
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
        ArrayList<Player> transfered = new ArrayList<>();
        for (int i=0; i < index ; i++) {
            transfered.add(players.get(i));
        }
        for (int i=0; i < index; i++) {
            players.remove(0);
        }
        players.addAll(transfered);
    }

    public boolean allDropped() {
        for (int i=1; i < players.size(); i++) {
            if (!players.get(i).isDropped()) {
                return false;
            }
        }
        return true;
    }

    /*
    * Undoes previous toss and returns its value. If can't remove a toss, because there are none,
    * returns -1. Skips dropped players unless a player threw third zero last round.
    *
    * */

    public int undo() {
        int removed_toss_points = -1;
        for (int i = 1; i < players.size(); i++) {
            int previous = players.get(players.size()- i).getTossesSize();
            int current = players.get(0).getTossesSize();
            if (previous > current) {
                removed_toss_points = players.get(players.size()-i).removeToss();
                setTurn(players.size()-i);
                return removed_toss_points;
            }
            else if ((previous > 0) && (previous == current) &&
                    (!players.get(players.size()-i).isDropped())) {
                removed_toss_points = players.get(players.size()-i).removeToss();
                setTurn(players.size()-i);
                return removed_toss_points;
            }
        }
        return  removed_toss_points;
    }

}
