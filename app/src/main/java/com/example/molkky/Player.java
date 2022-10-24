package com.example.molkky;

import java.util.ArrayList;
import java.util.Collections;

public class Player implements Comparable<Player> {
    private final String name;
    private final ArrayList<Integer> tosses;

    public Player(String name) {
        this.name = name;
        this.tosses = new ArrayList<>();
    }

    public boolean isEliminated() {
        return tosses.size() > 2 && getToss(tosses.size() - 1) == 0
                && getToss(tosses.size() - 2) == 0
                && getToss(tosses.size() - 3) == 0;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Integer> getTosses() {
        return tosses;
    }

    public int getTossesSize() { return tosses.size();}

    public int getToss(int position) { return tosses.get(position);}

    public void addToss(int points) {
        tosses.add(points);
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
        if (Boolean.compare(this.isEliminated(), player.isEliminated()) == 0)
        return Integer.compare(player.countAll(), this.countAll());
        else
            return Boolean.compare(this.isEliminated(), player.isEliminated());
    }

    public void clearTosses() {
        tosses.clear();
    }

    public float mean() {
        int sum = 0;
        for (int i : tosses) {
            sum += i;
        }
        return (float) sum / (float) tosses.size();
    }

    public int mode() {
        int max_freq = 0;
        int mode = 0;
        for (int i = 0; i < 13; i++) {
            int freq = Collections.frequency(tosses, i);
            if (freq > max_freq)
                mode = i;
        }
        return mode;
    }

    public int countExcesses() {
        int excesses = 0;
        for (int i=4; i < tosses.size(); i++) {
            if(count(i) == 25 && count(i-1) > 25)
                excesses++;
        }
        return excesses;
    }

    public int excessesTargetPoints(int round) {
        if (round > 3 && count(round) == 25 && count(round -1) > 25)
            return 1;
        else
            return 0;
    }

}
