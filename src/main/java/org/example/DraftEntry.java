package org.example;

public class DraftEntry<V> {
    private V value;
    private long currentTime;
    private boolean isRemove;
    private boolean isFullRemove;

    public DraftEntry(V value, long timestamp) {
        this.value = value;
        this.currentTime = timestamp;
        this.isRemove = false;
    }

    public DraftEntry(boolean isFullRemove, long timestamp) {
        this.value = null;
        this.currentTime = timestamp;
        this.isRemove = true;
        this.isFullRemove = isFullRemove;
    }

    public V getValue() {
        return value;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public boolean isRemove() {
        return isRemove;
    }

    public boolean isFullRemove() {
        return isFullRemove;
    }
}
