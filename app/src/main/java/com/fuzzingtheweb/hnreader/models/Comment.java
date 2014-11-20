package com.fuzzingtheweb.hnreader.models;

import java.util.ArrayList;

public class Comment {

    private int index;
    private String by;
    private long id;
    private long parent;
    private ArrayList<String> kids;
    private long time;
    private String text;

    public Comment(int index, String by, long id, long parent, ArrayList<String> kids, long time, String text) {
        this.index = index;
        this.by = by;
        this.id = id;
        this.parent = parent;
        this.kids = kids;
        this.time = time;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    public ArrayList<String> getKids() {
        return kids;
    }

    public void setKids(ArrayList<String> kids) {
        this.kids = kids;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
