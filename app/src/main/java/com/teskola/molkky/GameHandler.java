package com.teskola.molkky;

import android.content.Context;


import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameHandler {
    private GameListener listener;
    private Game game;
    private final FirebaseManager firebaseManager;
    private final boolean postTosses;
    private List<Toss> liveTosses = new ArrayList<>();
    private String liveId;
    private long liveGameTimestamp = 0;

    public static class LiveGame {
        private List<Player> players;
        private long started;
        private List<Toss> tosses;
        public LiveGame () {
        }

        public List<Player> getPlayers() {
            return players;
        }

        public long getStarted() {
            return started;
        }

        public List<Toss> getTosses() {
            return tosses;
        }
    }


    private final FirebaseManager.LiveGameListener liveGameListener = liveGame -> {
        if (liveGameTimestamp == 0)
            liveGameTimestamp = liveGame.getStarted();
        else if (liveGameTimestamp != liveGame.getStarted()) {
            listener.onNewGameStarted(liveGame.getPlayers(), liveId);
            return;
        }
        List<Toss> newTosses = liveGame.getTosses();
        if (newTosses == null) {
            while (liveTosses.size() > 0)
                removeToss();
            return;
        }
        while (newTosses.size() > liveTosses.size()) {
            Toss toss = newTosses.get(liveTosses.size());
            if (!toss.getPid().equals(current().getId()))
                break;
            addToss(toss.getValue());
        }
        while (newTosses.size() < liveTosses.size()) {
            removeToss();
        }
    };

    private List<Toss> getLiveTosses (Game game) {
        int totalTosses = game.getTossesCount();
        List<Toss> tosses = new ArrayList<>(game.getTossesCount());
        while (game.getTossesCount() > 0) {
            for (int i = 1; i < game.getPlayers().size(); i++) {
                Player previous = game.getPlayer(game.getPlayers().size() - i);
                Player current = game.getPlayer(0);
                if ((previous.getTosses().size() > current.getTosses().size()) || !previous.isEliminated()) {
                    previous.getUndoStack().push(previous.removeToss());
                    game.setTurn(game.getPlayers().size() - i);
                    break;
                }
            }
        }
        for (int i = 0; i < totalTosses; i++) {
            Toss toss = new Toss(game.getPlayer(0).getId(), game.getPlayer(0).getUndoStack().peek());
            tosses.add(toss);
            game.addToss(game.getPlayer(0).getUndoStack().pop());
        }
        return tosses;
    }



    // Saved state

    public GameHandler (Context context, String gameJson, String liveId, GameListener gameListener) {
        this.firebaseManager = FirebaseManager.getInstance(context);
        this.listener = gameListener;
        this.liveId = liveId;
        Game savedGame = new Gson().fromJson(gameJson, Game.class);
        if (gameListener != null) // saved state, not saved game
            this.liveTosses = getLiveTosses(savedGame);
        this.game = savedGame;
        this.postTosses = liveId == null;
        if (liveId != null) {
            startFetchingLiveData(liveId);
        }
    }

    // New spectator

    public GameHandler (Context context, List<Player> players, String liveId, GameListener gameListener) {
        this.firebaseManager = FirebaseManager.getInstance(context);
        this.listener = gameListener;
        this.liveId = liveId;
        this.game = new Game(players);
        this.postTosses = false;
        startFetchingLiveData(liveId);
    }


    // New game

    public GameHandler (Context context, String playersJson, boolean random, GameListener gameListener) {
        this.firebaseManager = FirebaseManager.getInstance(context);
        this.listener = gameListener;

        Player[] players = new Gson().fromJson(playersJson, Player[].class);
        ArrayList<Player> playersList = new ArrayList<>();
        Collections.addAll(playersList, players);

        this.game = new Game(playersList, random);
        if (FirebaseAuth.getInstance().getUid() != null) {  // TODO authstatelistener
            firebaseManager.addLiveGame(game);
            this.postTosses = true;
        }
        else
            this.postTosses = false;
    }

    public void close() {
        if (liveId != null)
            firebaseManager.removeLiveGameListener(liveId);
    }

    public Game getGame () {
        return game;
    }

    public void setGame (Game game) {
        this.game = game;
    }

    public String getLiveId () {
        return liveId != null ? liveId : firebaseManager.getLiveGameId();
    }

    public interface GameListener {
        void onTurnChanged(int points, int chanceToWin, boolean undo);
        void onGameStatusChanged(boolean gameEnded);
        void onNewGameStarted(List<Player> players, String liveId);
    }

    public void startFetchingLiveData (String gameId) {
        firebaseManager.setLiveGameListener(gameId, liveGameListener);
    }

    public int getColor () { return Colors.selectBackground(game.getPlayer(0), false);}

    public boolean gameEnded() {
        return (game.getPlayer(0).countAll() == 50 || game.allDropped());
    }

    public Player current () {
        return game.getPlayer(0);
    }

    public List<Player> getPlayers (boolean sorted) {
        if (!sorted)
            return game.getPlayers();
        ArrayList<Player> sortedPlayers = new ArrayList<>(game.getPlayers());
        Collections.sort(sortedPlayers);
        return sortedPlayers;
    }

    public boolean undoStackIsEmpty () {
        return game.getPlayer(0).getUndoStack().empty();
    }

    public long getSeekbarPosition () {
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
    public long getUndoStackValue () {
        if (undoStackIsEmpty()) return -1;
        return game.getPlayer(0).getUndoStack().peek();
    }

    public long getLastToss () {
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

    public void addToss(long points) {
        liveTosses.add(new Toss(game.getPlayer(0).getId(), points));
        if (postTosses)
            firebaseManager.postTosses(liveTosses);
        if (!game.getPlayer(0).getUndoStack().empty())
            game.getPlayer(0).getUndoStack().pop();
        game.getPlayer(0).addToss(points);
        if (game.getPlayer(0).countAll() == 50) {
            endGame();
            return;
        }
        game.setTurn(1);
        listener.onTurnChanged((int) getUndoStackValue(), game.getPlayer(0).pointsToWin(), false);
        while (game.getPlayer(0).isEliminated()) {
            game.setTurn(1);
            listener.onTurnChanged((int) getUndoStackValue(), game.getPlayer(0).pointsToWin(), false);
        }
        if (game.allDropped()) {
            endGame();
        }

    }

    public boolean noTosses () {
        return game.getTossesCount() == 0;
    }

    public int last () {
        return game.getPlayers().size() - 1;
    }

    public void removeToss () {
        liveTosses.remove(liveTosses.size() - 1);
        if (postTosses)
            firebaseManager.postTosses(liveTosses);
        if (game.getPlayer(0).countAll() == 50) {
            long removed = game.getPlayer(0).removeToss();
            game.getPlayer(0).getUndoStack().push(removed);
            listener.onGameStatusChanged(false);
            return;
        }
        boolean allDropped = game.allDropped();
        for (int i=1; i < game.getPlayers().size(); i++) {
            Player previous = game.getPlayer(game.getPlayers().size() - i);
            Player current = game.getPlayer(0);
            if (previous.getTosses().size() > current.getTosses().size() || !previous.isEliminated()) {
                long removed = previous.getUndoStack().push(previous.removeToss());
                int chanceToWin = previous.pointsToWin();
                for (int j=0; j < i; j++) {
                    game.setTurn(game.getPlayers().size() - 1);
                    listener.onTurnChanged((int) removed, chanceToWin, true);
                    if (allDropped) listener.onGameStatusChanged(false);
                }
                break;
            }
        }
    }

    public void endGame () {
        if (postTosses)
            firebaseManager.addGameToDatabase(game);
        listener.onGameStatusChanged(true);
    }
}
