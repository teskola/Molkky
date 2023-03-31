package com.teskola.molkky;

public class PlayerInfo {
    private String id;
    private String name;
    private boolean image = false;

    public PlayerInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean hasImage() {
        return image;
    }

    public void setImage(boolean image) {
        this.image = image;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerInfo that = (PlayerInfo) o;
        return id.equals(that.id);
    }
}
