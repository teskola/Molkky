package com.teskola.molkky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Objects;

public class LocalDatabaseManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "molkky_db";
    private static final int DB_VERSION = 1;
    private static LocalDatabaseManager instance;

    private LocalDatabaseManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static LocalDatabaseManager getInstance(Context context) {
        if (instance == null)
            instance = new LocalDatabaseManager(context.getApplicationContext());
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String games = ""
                + "CREATE TABLE 'games' ( "
                + "	'id'	TEXT NOT NULL PRIMARY KEY, "
                + "	'winner'	INTEGER NOT NULL, "
                + " 'time' TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "	FOREIGN KEY('winner') REFERENCES 'players'('id'));";


        String players = ""
                + "CREATE TABLE 'players' ( "
                + "	'id'	TEXT NOT NULL PRIMARY KEY, "
                + "	'name' TEXT NOT NULL)";


        String tosses = ""
                + "CREATE TABLE 'tosses' ( "
                + "	'gameId'	INTEGER NOT NULL, "
                + "	'toss'	INTEGER , "
                + "	'playerId'	TEXT NOT NULL, "
                + "	FOREIGN KEY('playerId') REFERENCES 'players'('id'), "
                + "	FOREIGN KEY('gameId') REFERENCES 'games'('id') ON DELETE CASCADE);";


        sqLiteDatabase.execSQL(games);
        sqLiteDatabase.execSQL(players);
        sqLiteDatabase.execSQL(tosses);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS 'games'");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS 'players'");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS 'tosses'");
        onCreate(sqLiteDatabase);

    }
    @Override
    public void onOpen(SQLiteDatabase sqLiteDatabase) {
        super.onOpen(sqLiteDatabase);
        sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON;");
    }

    public String getPlayerId(String name) {
        ArrayList<PlayerInfo> players = getPlayers();
        for (PlayerInfo player : players)
            if (player.getName().equals(name)) return player.getId();
        return null;
    }

    public ArrayList<String> getGameIds(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT gameId FROM tosses WHERE playerId= '" + playerId + "'", null);
        ArrayList<String> gameIds = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                gameIds.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return gameIds;
    }

    public int getWins(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT (id) FROM games WHERE winner='" + playerId + "'", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getTotalPoints(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(toss) FROM tosses WHERE playerId='" + playerId + "'", null);
        cursor.moveToFirst();
        int sum = cursor.getInt(0);
        cursor.close();
        return sum;
    }

    public int getTotalTosses(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(toss) FROM tosses WHERE playerId='" + playerId + "'", null);
        cursor.moveToFirst();
        int sum = cursor.getInt(0);
        cursor.close();
        return sum;
    }

    public int countTosses(String playerId, int value) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(toss) FROM tosses WHERE playerId='" + playerId + "'" + " AND toss=" + value, null);
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        cursor.close();
        return result;
    }

    public boolean isEliminated(String playerId, String gameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT toss FROM tosses WHERE playerId='" + playerId + "'" + " AND gameId= '" + gameId + "' ORDER BY ROWID DESC LIMIT 3;", null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(0) != 0) {
                    cursor.close();
                    return false;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return true;
    }

    public ArrayList<GameInfo> getGames(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT gameId, strftime('%d-%m-%Y %H:%M', time, 'localtime'), name FROM tosses " +
                "LEFT JOIN games ON gameId=games.id " +
                "LEFT JOIN players ON games.winner=players.id " +
                "WHERE playerId='" + playerId + "' ORDER BY gameId DESC", null);

        ArrayList<GameInfo> games = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                GameInfo newGame = new GameInfo(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                games.add(newGame);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return games;

    }

    public ArrayList<GameInfo> getGames() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT games.id, strftime('%d-%m-%Y %H:%M', time, 'localtime'), " +
                "name FROM games LEFT JOIN players ON games.winner = players.id ORDER BY games.id DESC", null);
        ArrayList<GameInfo> games = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                GameInfo newGame = new GameInfo(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                games.add(newGame);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return games;
    }

    public String getPlayerName(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM players WHERE id='" + playerId + "'", null);
        cursor.moveToFirst();
        String name = cursor.getString(0);
        cursor.close();
        return name;
    }

    public ArrayList<String> getPlayerNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM players", null);
        ArrayList<String> names = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                names.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return names;
    }


    public ArrayList<PlayerInfo> getPlayers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM players", null);
        ArrayList<PlayerInfo> players = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                players.add(new PlayerInfo(cursor.getString(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return players;
    }

    public ArrayList<Player> getPlayers(String gameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT playerId, name FROM tosses LEFT JOIN players ON playerId=players.id  WHERE gameId='" + gameId + "'", null);
        ArrayList<Player> players = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String playerId = cursor.getString(0);
                String name = cursor.getString(1);
                players.add(new Player(playerId, name, getTosses(gameId, playerId)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return players;
    }


    public ArrayList<PlayerInfo> getPlayers(ArrayList<PlayerInfo> excludedPlayers) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name FROM players", null);
        ArrayList<PlayerInfo> players = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                boolean duplicate = false;
                for (PlayerInfo player : excludedPlayers) {
                    if (Objects.equals(player.getId(), cursor.getString(0))) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate)
                    players.add(new PlayerInfo(cursor.getString(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return players;
    }

    public int getTossesCount(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT (*) FROM tosses WHERE playerId='" + playerId + "'", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public ArrayList<Integer> getTosses(String gameId, String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT toss FROM tosses WHERE playerId='" + playerId + "'" + " AND gameId= '" + gameId + "'", null);
        ArrayList<Integer> tosses = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                tosses.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tosses;
    }

    public int getGamesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM games", null);
        cursor.moveToFirst();
        int size = cursor.getInt(0);
        cursor.close();
        return size;

    }

    public int getPlayerCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM players", null);
        cursor.moveToFirst();
        int size = cursor.getInt(0);
        cursor.close();
        return size;

    }

    /*
    *
    *  Removes game from database and players who are not in any game.
    *
    * */

    public void removeGameFromDatabase(String gameId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM games WHERE id = '" + gameId + "';"); // cascade deletes also tosses
        db.execSQL("DELETE FROM players WHERE id IN (SELECT id FROM players EXCEPT SELECT playerId FROM tosses)");
    }

    /*
    *
    * Saves game to local database. If game with same id is already in database, overwrite.
    *
    * */

    public void saveGameToDatabase(Game game) {

        removeGameFromDatabase(game.getId());

        SQLiteDatabase db = this.getWritableDatabase();

        // Add player to database if player is already in database throws SQLiteConstraintException

        for (Player player : game.getPlayers()) {

            ContentValues values = new ContentValues();
            values.put("name", player.getName());
            values.put("id", player.getId());
            db.insert("players", null, values);

        }

        // Add game to database

        String winner = game.getPlayers().get(0).getId();
        ContentValues values = new ContentValues();
        values.put("winner", winner);
        values.put("id", game.getId());
        db.insert("games", null, values);

        // Add tosses to database

        values.clear();
        values.put("gameId", game.getId());
        for (Player p : game.getPlayers()) {
            values.put("playerId", p.getId());
            for (int i : p.getTosses()) {
                values.put("toss", i);
                db.insert("tosses", null, values);
            }
        }
    }
}
