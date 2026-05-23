package com.flycast.emulator;

public class FileInfo {
    private String path;
    private String name;
    private boolean directory;

    // ✅ extra fields required by their AndroidStorage
    private long size;
    private boolean writable;
    private long updateTime;

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDirectory() { return directory; }
    public void setDirectory(boolean directory) { this.directory = directory; }

    // ✅ missing setters required by AndroidStorage
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public boolean isWritable() { return writable; }
    public void setWritable(boolean writable) { this.writable = writable; }

    public long getUpdateTime() { return updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }
}

