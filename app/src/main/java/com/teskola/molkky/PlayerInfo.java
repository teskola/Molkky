package com.teskola.molkky;

import android.graphics.Bitmap;

public class PlayerInfo {

    private int id = 0;
    private String name;

    public PlayerInfo(int id, String name) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
