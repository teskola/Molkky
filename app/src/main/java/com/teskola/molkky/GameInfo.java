package com.teskola.molkky;


import java.text.SimpleDateFormat;
import java.util.Locale;

public class GameInfo {
    private String data;
    private String timestamp;
    private String winner;
    private int id = 0;

    public GameInfo(int id, String timestamp, String winner) {
        this.id = id;
        this.winner = winner;
        this.timestamp = timestamp;
        this.data = this.timestamp + " (" + this.winner + ")";
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

