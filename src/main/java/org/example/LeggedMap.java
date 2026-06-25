package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeggedMap<K, V> {
    private int draftSeconds;
    private Map<K, V> publishedMap;
    private Map<K, DraftEntry<V>> draftsMap;
    private Map<K, List<K>> historyMap;

    public LeggedMap(int draftSeconds) {
        this.draftSeconds = draftSeconds;
        this.publishedMap = new HashMap<>();
        this.draftsMap = new HashMap<>();
        this.historyMap = new HashMap<>();
    }

    public void put(K key, V value) {
        long currentTime = System.currentTimeMillis();
        DraftEntry<V> newDraft = new DraftEntry<>(value, currentTime);
        this.draftsMap.put(key, newDraft);
    }

    public V get(K key) {
        return this.draftsMap.get(key).getValue();
    }
}
