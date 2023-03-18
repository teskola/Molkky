package com.teskola.molkky;

import com.google.firebase.database.Exclude;


public class PlayerInfo {
    @Exclude
    private String id;
    private String name;

    public PlayerInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PlayerInfo(String name) {
        this.name = name;
    }

    public PlayerInfo() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
