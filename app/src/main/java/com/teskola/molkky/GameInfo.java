package com.teskola.molkky;

public class GameInfo {
    private String data;
    private final String timestamp;
    private final String winner;
    private String id;

    public GameInfo(String id, String timestamp, String winner) {
        this.id = id;
        this.winner = winner;
        this.timestamp = timestamp;
        this.data = this.timestamp + " (" + this.winner + ")";
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getData() { return data;}
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


}

