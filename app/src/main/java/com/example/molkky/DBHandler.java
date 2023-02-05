package com.example.molkky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Objects;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "molkky_db";
    private static final int DB_VERSION = 1;
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String games = ""
                + "CREATE TABLE \"games\" ( "
                + "	\"id\"	INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "	\"winner\"	INTEGER, "
                + "	FOREIGN KEY(\"winner\") REFERENCES \"players\"(\"id\"));";


        String players = ""
                + "CREATE TABLE \"players\" ( "
                + "	\"id\"	INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "	\"name\"	TEXT UNIQUE)";


        String tosses = ""
                + "CREATE TABLE \"tosses\" ( "
                + "	\"id\"	INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "	\"gameId\"	INTEGER, "
                + "	\"toss\"	INTEGER, "
                + "	\"playerId\"	INTEGER, "
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

    public boolean searchPlayerName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM players", null);

        if (cursor.moveToFirst()) {
            do {
                if (name.equals(cursor.getString(0))) return true;
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return false;
    }

    public ArrayList<Player> readPlayers(ArrayList<Player> excludedPlayers) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM players", null);
        ArrayList<Player> players = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                boolean duplicate = false;
                for (Player player : excludedPlayers) {
                    if (Objects.equals(player.getName(), cursor.getString(0))) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) players.add(new Player(cursor.getString(0)));
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return players;
    }

    public int playersTableSize () {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM players", null);
        cursor.moveToFirst();
        int size = cursor.getInt(0);
        db.close();
        cursor.close();
        return size;

    }

    public void removeGameFromDatabase (Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        String games = "DELETE FROM games WHERE id = " + game.getId() + ";";
        String tosses = "DELETE FROM tosses WHERE gameId = " + game.getId() + ";";
        db.execSQL(games);
        db.execSQL(tosses);
        db.close();
    }

    public void saveGameToDatabase(Game game) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        // Add player to database

        for (Player player : game.getPlayers()) {
            ContentValues values = new ContentValues();
            values.put("name", player.getName());
            db.insert("players", null, values);
            cursor = db.rawQuery("SELECT id FROM players WHERE name= \"" + player.getName() + "\";", null);
            cursor.moveToFirst();
            player.setId(cursor.getInt(0));
        }

        // Add game to database

        int winner = game.getPlayers().get(0).getId();
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
        db.close();
    }

}
