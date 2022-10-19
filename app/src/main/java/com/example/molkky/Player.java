package com.example.molkky;

import java.util.ArrayList;

public class Player {
    private String name;
    private ArrayList<Integer> tosses;
    private boolean IAmDropped;

    public Player(String name) {
        this.name = name;
        this.tosses = new ArrayList<>();
        this.IAmDropped = false;
    }

    public boolean isDropped() {
        return IAmDropped;
    }

    public String getName() {
        return name;
    }

    public int getTossesSize() { return tosses.size();}

    public void addToss(int points) {
        IAmDropped = false;         // varmistetaan, ettei undo() metodin jälkeen jää väärä putoamis-status roikkumaan
        tosses.add(points);
        int size = tosses.size();
        if (points == 0 && size > 2) {
            if (tosses.get(size - 2) == 0 &&
                    tosses.get(size-3) == 0) {
                IAmDropped = true;
            }
        }
    }

    public int removeToss() {
        int removed_toss_points = tosses.get(tosses.size() -1);
        tosses.remove(tosses.size() - 1);
        return  removed_toss_points;
    }

    public int count(int round) {
        int sum = 0;
        for (int i=0; i <= round; i++) {
            sum += tosses.get(i);
            if (sum > 50) {
                sum = 25;
            }
        }
        return sum;
    }

    public int countAll() {
        return count (tosses.size() - 1);
    }

}
