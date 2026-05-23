package com.flycast.wrapper;

public class GameEntry {

    public enum Type {
        GAME,
        TOOL_BIOS
    }

    private String title;
    private String romPath;
    private String coverPath;
    private Type type = Type.GAME;

    public GameEntry(String title, String romPath, String coverPath) {
        this.title = title;
        this.romPath = romPath;
        this.coverPath = coverPath;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRomPath() { return romPath; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public Type getType() { return type; }
    public void setType(Type t) { this.type = t; }
}