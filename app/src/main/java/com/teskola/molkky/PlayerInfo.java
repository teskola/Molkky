package com.teskola.molkky;

import com.google.firebase.database.Exclude;

public class PlayerInfo {
    private String id;
    private String name;
    @Exclude
    private String alterEgo;

    public PlayerInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public PlayerInfo(PlayerInfo playerInfo) {
        this.id = playerInfo.id;
        this.name = playerInfo.name;
        this.alterEgo = playerInfo.alterEgo;
    }

    public void setAlterEgo (String alterEgo) {
        this.alterEgo = alterEgo;
    }

    public PlayerInfo(String name) {
        this.name = name;
    }

    public PlayerInfo() {}

    public String getName() {
        return (alterEgo != null ? alterEgo : name);
    }

    @Exclude
    public String getNameInDatabase() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerInfo that = (PlayerInfo) o;
        return id.equals(that.id);
    }
}
