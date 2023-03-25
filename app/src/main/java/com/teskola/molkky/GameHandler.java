package com.teskola.molkky;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;

public class GameHandler {
    private transient GameListener listener;
    private Game game;
    private final DatabaseHandler databaseHandler;
    private boolean livescore = true;

    public GameHandler (Context context) {
        this.databaseHandler = DatabaseHandler.getInstance(context);
    }

    public void setGame (Game game) {
        this.game = game;
    }

    public void setListener (GameListener listener) {
        this.listener = listener;
    }

    public Game getGame () {
        return game;
    }

    public static String getLiveId (String gameId) {
        return gameId.substring(4,8);
    }

    public interface GameListener {
        void onTurnChanged(int points, int chanceToWin, boolean undo);
        void onGameStatusChanged(boolean gameEnded);
    }

    public void startPostingLiveData () {
        databaseHandler.startGame(game);
    }

    public int getColor () { return Colors.selectBackground(game.getPlayer(0), false);}

    public boolean gameEnded() {
        return (game.getPlayer(0).countAll() == 50 || game.allDropped());
    }

    public Player current () {
        return game.getPlayer(0);
    }

    public ArrayList<Player> getPlayers (boolean sorted) {
        if (!sorted)
            return game.getPlayers();
        ArrayList<Player> sortedPlayers = new ArrayList<>(game.getPlayers());
        Collections.sort(sortedPlayers);
        return sortedPlayers;
    }

    public boolean undoStackIsEmpty () {
        return game.getPlayer(0).getUndoStack().empty();
    }

    public int getSeekbarPosition () {
        if (game.getPlayer(0).getUndoStack().empty())
            return GameActivity.SEEKBAR_DEFAULT_POSITION;
        else
            return game.getPlayer(0).getUndoStack().peek();
    }
/*
*
*   Returns -1, if undoStack is empty.
*
* */
    public int getUndoStackValue () {
        if (undoStackIsEmpty()) return -1;
        return game.getPlayer(0).getUndoStack().peek();
    }

    public int getLastToss () {
        if (game.getPlayer(0).countAll() == 50)
            return game.getPlayer(0).getToss(game.getPlayer(0).getTosses().size() - 1);
        else
            return 0;
    }

    public String getPlayerName () {
        return game.getPlayer(0).getName();
    }

    public int getPointsToWin () {
        return game.getPlayer(0).pointsToWin();
    }

    public void addToss(int points) {
        if (livescore)
            databaseHandler.addToss(getLiveId(game.getId()), game.getTossesCount(), points);
        if (!game.getPlayer(0).getUndoStack().empty())
            game.getPlayer(0).getUndoStack().pop();
        game.getPlayer(0).addToss(points);
        if (game.getPlayer(0).countAll() == 50) {
            endGame();
            return;
        }
        game.setTurn(1);
        listener.onTurnChanged(getUndoStackValue(), game.getPlayer(0).pointsToWin(), false);
        while (game.getPlayer(0).isEliminated()) {
            game.setTurn(1);
            listener.onTurnChanged(getUndoStackValue(), game.getPlayer(0).pointsToWin(), false);
        }
        if (game.allDropped()) {
            endGame();
        }

    }

    public boolean noTosses () {
        return game.getPlayer(game.getPlayers().size() - 1).getTosses().size() == 0;
    }

    public int last () {
        return game.getPlayers().size() - 1;
    }

    public void removeToss () {
        if (livescore)
            databaseHandler.removeToss(getLiveId(game.getId()), game.getTossesCount() - 1);
        if (game.getPlayer(0).countAll() == 50) {
            int removed = game.getPlayer(0).removeToss();
            game.getPlayer(0).getUndoStack().push(removed);
            listener.onGameStatusChanged(false);
            return;
        }
        boolean allDropped = game.allDropped();
        for (int i=1; i < game.getPlayers().size(); i++) {
            Player previous = game.getPlayer(game.getPlayers().size() - i);
            Player current = game.getPlayer(0);
            if (previous.getTosses().size() > current.getTosses().size() || !previous.isEliminated()) {
                int removed = previous.getUndoStack().push(previous.removeToss());
                int chanceToWin = previous.pointsToWin();
                for (int j=0; j < i; j++) {
                    game.setTurn(game.getPlayers().size() - 1);
                    listener.onTurnChanged(removed, chanceToWin, true);
                    if (allDropped) listener.onGameStatusChanged(false);
                }
                break;
            }
        }
    }

    public void endGame () {
        databaseHandler.saveGame(game);
        listener.onGameStatusChanged(true);
    }
}
