package com.example.molkky;

import java.util.ArrayList;

public class Player implements Comparable<Player> {
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

    public int getToss(int position) { return tosses.get(position);}

    public void addToss(int points) {
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

    /*
    * If player has a chance to win with next toss, returns the value needed.
    * Else, returns 0.
    * */

    public int pointsToWin() {
        int toWin = 0;
        int total = countAll();
        if (total < 38)
            return toWin;
        else
            return 50 - total;
    }

    @Override
    public int compareTo(Player player) {
        if (Boolean.compare(this.IAmDropped, player.IAmDropped) == 0)
        return Integer.compare(player.countAll(), this.countAll());
        else
            return Boolean.compare(this.IAmDropped, player.IAmDropped);
    }

    public void clear() {
        IAmDropped = false;
        tosses.clear();
    }

}
