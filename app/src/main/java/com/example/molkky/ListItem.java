package com.example.molkky;

public class ListItem implements Comparable<ListItem> {
    private String name;
    private float valueFloat = 0f;
    private int valueInt = 0;
    private int id = 0;
    public ListItem(String name, float value) {
        this.name = name;
        this.valueFloat = value;
    }
    public ListItem(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public ListItem() {
        this.name = "";
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

    public int getValueInt() {
        return valueInt;
    }

    public void setValueInt(int valueInt) {
        this.valueInt = valueInt;
    }

    public float getValueFloat() {
        return valueFloat;
    }

    public void setValueFloat(float valueFloat) {
        this.valueFloat = valueFloat;
    }

    @Override
    public int compareTo(ListItem listItem) {
        if (listItem.valueInt == this.valueInt)
            return Float.compare(listItem.valueFloat, this.valueFloat);
        else
            return Integer.compare(listItem.valueInt, this.valueInt);
    }

}

