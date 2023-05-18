package com.teskola.molkky;

public class Toss {
    private String pid;
    private Long value;
    public Toss () {}

    public Toss (String pid, Long value) {
        this.pid = pid;
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public String getPid() {
        return pid;
    }
}
