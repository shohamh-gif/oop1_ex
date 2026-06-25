package org.example;

public class DraftEntry <V>{
    private V value;
    private long currentTime;

    public DraftEntry(V value, long timestamp) {
        this.value = value;
        this.currentTime = timestamp;
    }

    public V getValue() {
        return value;
    }

    public long getCurrentTime() {
        return currentTime;
    }

}
