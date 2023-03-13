package com.teskola.molkky;

public class DatabaseUser {
    private String id;
    private boolean connected;
    private long joined;
    private long lastConnect;

    public DatabaseUser(String id, boolean connected, long joined, long lastConnect) {
        this.id = id;
        this.connected = connected;
        this.joined = joined;
        this.lastConnect = lastConnect;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public long getJoined() {
        return joined;
    }

    public void setJoined(long joined) {
        this.joined = joined;
    }

    public long getLastConnect() {
        return lastConnect;
    }

    public void setLastConnect(long lastConnect) {
        this.lastConnect = lastConnect;
    }
}
