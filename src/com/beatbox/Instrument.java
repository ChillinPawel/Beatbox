package com.beatbox;

public class Instrument {
    private String name;
    private int id;

    public Instrument(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
