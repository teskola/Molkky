package com.teskola.molkky;

import java.util.UUID;

public class PlayerInfo {

    private String id;
    private String name;

    public PlayerInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PlayerInfo(String name) {
        this.name = name;
        this.id = UUID.randomUUID().toString().substring(0,8);
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
