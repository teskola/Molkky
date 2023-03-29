package com.teskola.molkky;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Player extends PlayerInfo implements Comparable<Player> {

    private List<Long> tosses;
    @Exclude
    private Stack<Long> undoStack;

    public Player(String id, String name, List<Long> tosses) {
        super(id, name);
        this.tosses = tosses;
        this.undoStack = new Stack<>();
    }

    public Player(List<Long> tosses) {
        super();
        this.tosses = tosses;

    }

    public Player() {
        tosses = new ArrayList<>();
    }



    @Exclude
    public boolean isEliminated() {
        return tosses.size() > 2 && getToss(tosses.size() - 1) == 0
                && getToss(tosses.size() - 2) == 0
                && getToss(tosses.size() - 3) == 0;
    }


    /*
    * Initializes undoStack if not initialized and returns undoStack.
    * */
    @Exclude
    public Stack<Long> getUndoStack() {
        if (undoStack == null) {
            undoStack = new Stack<>();
        }
        return undoStack;
    }

    public List<Long> getTosses() {
        return tosses;
    }

    public long getToss(int round) { return tosses.get(round);}

    public void addToss(long points) {
        tosses.add(points);
    }
/*
* Removes player's last toss. Returns value of the removed toss.
* */
    public long removeToss() {
        long removed_toss_points = tosses.get(tosses.size() -1);
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

    /*
    * Returns players current points. Initializes tosses array if not initialized.
    * */

    public int countAll() {
        if (tosses == null) {
            tosses = new ArrayList<>();
        }
        return count (tosses.size() - 1);
    }

    /*
    * If player has a chance to win with next toss, returns the value needed.
    * Else, returns 0.
    * */

    public int pointsToWin() {
        int total = countAll();
        if (total < 38)
            return 0;
        else
            return 50 - total;
    }

    public boolean winningChance(int round) {
        int total = count(round);
        return (total > 37);
    }

    @Override
    public int compareTo(Player player) {
        if (Boolean.compare(this.isEliminated(), player.isEliminated()) == 0)
        return Long.compare(player.countAll(), this.countAll());
        else
            return Boolean.compare(this.isEliminated(), player.isEliminated());
    }

/*
* ******************* Stats
* */

    public int hits() {
        int count = 0;
        for (long i : getTosses()) {
            if (i != 0) count++;
        }
        return count;
    }

    public int hitsPct() {
        return Math.round(100 * (float) hits() / (float) getTosses().size());
    }

    public float mean() {
        int sum = 0;
        for (long i : tosses) {
            sum += i;
        }
        return (float) sum / (float) tosses.size();
    }

    public int countWinningChances () {
        int count = 0;
        for (int i = tosses.size()-2; i > 2; i--) {
            if (winningChance(i)) count++;
        }
        return count;
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
