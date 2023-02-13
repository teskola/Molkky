package com.example.molkky;

public class PlayerInfo {

    private int id = 0;
    private String name;
    private int image = 0;

    public PlayerInfo(int id, String name, int image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public PlayerInfo(String name) {
        this.name = name;
    }

    public PlayerInfo() {
    }

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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }


}
