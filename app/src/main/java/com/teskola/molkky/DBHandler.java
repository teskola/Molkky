package com.teskola.molkky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "molkky_db";
    private static final int DB_VERSION = 1;
    private static DBHandler instance;

    private DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DBHandler getInstance(Context context) {
        if (instance == null)
            instance = new DBHandler(context);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String games = ""
                + "CREATE TABLE \"games\" ( "
                + "	\"id\"	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + "	\"winner\"	INTEGER, "
                + " \"time\" STRFTIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "	FOREIGN KEY(\"winner\") REFERENCES \"players\"(\"id\"));";


        String players = ""
                + "CREATE TABLE \"players\" ( "
                + "	\"id\"	TEXT NOT NULL PRIMARY KEY, "
                + "	\"name\" TEXT NOT NULL)";



        String tosses = ""
                + "CREATE TABLE \"tosses\" ( "
                + "	\"id\"	INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "	\"gameId\"	INTEGER NOT NULL, "
                + "	\"toss\"	INTEGER NOT NULL, "
                + "	\"playerId\"	TEXT NOT NULL, "
                + "	FOREIGN KEY(\"playerId\") REFERENCES \"players\"(\"id\"), "
                + "	FOREIGN KEY(\"gameId\") REFERENCES \"game\"(\"id\"));";




        sqLiteDatabase.execSQL(games);
        sqLiteDatabase.execSQL(players);
        sqLiteDatabase.execSQL(tosses);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS \"games\"");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS \"players\"");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS \"tosses\"");
        onCreate(sqLiteDatabase);

    }

    public boolean PlayerNameIsOnDatabase(String name) {
        ArrayList<String> dbNames = getPlayerNames();
        for (String dbName : dbNames)
            if (dbName.equals(name)) return true;
        return false;
    }

    public ArrayList<Integer> getGameIds(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT gameId FROM tosses WHERE playerId= \"" + playerId + "\"", null);
        ArrayList<Integer> gameIds = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                gameIds.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return gameIds;
    }

    public int getWins(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT (id) FROM games WHERE winner=\"" + playerId + "\"", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getTotalPoints (String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(toss) FROM tosses WHERE playerId=\"" + playerId + "\"", null);
        cursor.moveToFirst();
        int sum = cursor.getInt(0);
        cursor.close();
        return sum;
    }

    public int getTotalTosses (String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(toss) FROM tosses WHERE playerId=\"" + playerId + "\"", null);
        cursor.moveToFirst();
        int sum = cursor.getInt(0);
        cursor.close();
        return sum;
    }

    public int countTosses (String playerId, int value) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =db.rawQuery("SELECT COUNT(toss) FROM tosses WHERE playerId=\"" + playerId + "\"" + " AND toss=" + value, null);
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        cursor.close();
        return result;
    }

    public boolean isEliminated (String playerId, int gameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT toss FROM tosses WHERE playerId=\"" + playerId + "\"" + " AND gameId=" + gameId + " ORDER BY ROWID DESC LIMIT 3;", null);
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
        Cursor cursor = db.rawQuery("SELECT DISTINCT gameId, strftime('%d-%m-%Y %H:%M', time), name FROM tosses " +
                "LEFT JOIN games ON gameId=games.id " +
                "LEFT JOIN players ON games.winner=players.id " +
                "WHERE playerId=\"" + playerId + "\" ORDER BY gameId DESC", null);

        ArrayList<GameInfo> games = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                GameInfo newGame = new GameInfo(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                games.add(newGame);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return games;

    }

    public ArrayList<GameInfo> getGames() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT games.id, strftime('%d-%m-%Y %H:%M', time), name FROM games LEFT JOIN players ON games.winner = players.id ORDER BY games.id DESC", null);
        ArrayList<GameInfo> games = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                GameInfo newGame = new GameInfo(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                games.add(newGame);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return games;
    }

    public String getPlayerName(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM players WHERE id=\"" + playerId + "\"", null);
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

    public ArrayList<Player> getPlayers(int gameId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT playerId, name FROM tosses LEFT JOIN players ON playerId=players.id  WHERE gameId=" + gameId, null);
        ArrayList<Player> players = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String playerId = cursor.getString(0);
                String name = cursor.getString(1);
                players.add(new Player(playerId, name, getTosses(gameId, playerId)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  players;
    }


    public ArrayList<PlayerInfo> getPlayers(ArrayList<PlayerInfo> excludedPlayers) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM players", null);
        ArrayList<PlayerInfo> players = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                boolean duplicate = false;
                for (PlayerInfo player : excludedPlayers) {
                    if (Objects.equals(player.getName(), cursor.getString(0))) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) players.add(new PlayerInfo(cursor.getString(0)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return players;
    }

    public ArrayList<Integer> getTosses (int gameId, String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT toss FROM tosses WHERE playerId=\"" + playerId + "\"" + " AND gameId=" + gameId, null);
        ArrayList<Integer> tosses = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                tosses.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tosses;
    }

    public int getPlayerCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM players", null);
        cursor.moveToFirst();
        int size = cursor.getInt(0);
        cursor.close();
        return size;

    }

    public void removeGameFromDatabase (int gameId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String games = "DELETE FROM games WHERE id = " + gameId + ";";
        String tosses = "DELETE FROM tosses WHERE gameId = " + gameId + ";";
        db.execSQL(games);
        db.execSQL(tosses);
    }

    public void saveGameToDatabase(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        // Add player to database

        for (Player player : game.getPlayers()) {
            ContentValues values = new ContentValues();
            values.put("name", player.getName());
            values.put("id", player.getId());
            db.insert("players", null, values);
            cursor = db.rawQuery("SELECT id FROM players WHERE name= \"" + player.getName() + "\";", null);
            cursor.moveToFirst();
        }

        // Add game to database


        String winner = game.getPlayers().get(0).getId();
        ContentValues values = new ContentValues();
        values.put("winner", winner);
        db.insert("games", null, values);
        cursor = db.rawQuery("SELECT last_insert_rowid();", null);
        cursor.moveToFirst();
        game.setId(cursor.getInt(0));

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

        cursor.close();
    }

}
