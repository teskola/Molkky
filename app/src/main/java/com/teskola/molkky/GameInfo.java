package com.teskola.molkky;

public class GameInfo {
    private String data;
    private int id = 0;

    public GameInfo() {
        this.data = "";
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getData() { return data;}
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }


}

