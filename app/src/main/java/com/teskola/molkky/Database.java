package com.teskola.molkky;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Database {
    private String id = "";
    private long created;
    private String addedBy;
    private Map<String, Game> games;
    private ArrayList<DatabaseUser> users;

    public Database(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Database(String id, long created, String addedBy, Map<String, Game> games, List<DatabaseUser> users) {
        this.id = id;
        this.created = created;
        this.addedBy = addedBy;
        this.games = games;
        this.users = (ArrayList<DatabaseUser>) users;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

/*    public ArrayList<Game> getGames() {
        return games;
    }

    public void setGames(ArrayList<Game> games) {
        this.games = games;
    }*/

    /*public ArrayList<DatabaseUser> getUsers() {
        return users;
    }*/

    /*public void setUsers(ArrayList<DatabaseUser> users) {
        this.users = users;
    }*/

//    public Database databaseFromString (String id, String data) throws JSONException {
//        this.id = id;
//        JSONObject jsonObject = new JSONObject(data);
//        this.created = jsonObject.getLong("created");
//        this.addedBy = jsonObject.getString("addedBy");
//        jsonObject.
//
//        return this;
//    }
}
