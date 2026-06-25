package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaggedMap<K, V> {
    private int draftSeconds;
    private Map<K, V> publishedMap;
    private Map<K, DraftEntry<V>> draftsMap;
    private Map<K, List<V>> historyMap;

    public LaggedMap(int draftSeconds) {
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
        if (this.draftsMap.containsKey(key)) {
            DraftEntry<V> draft = this.draftsMap.get(key);
            long currentTime = System.currentTimeMillis();
            if (currentTime - draft.getCurrentTime() >= (this.draftSeconds * 1000L)) {
                V oldValue = this.publishedMap.get(key);
                if (oldValue != null) {
                    if (!this.historyMap.containsKey(key)) {
                        this.historyMap.put(key, new ArrayList<>());
                    } else {
                        List<V> historyList = this.historyMap.get(key);
                        historyList.add(oldValue);
                        if (historyList.size() > 3) {
                            historyList.remove(0);
                        }
                    }
                } else {
                    this.publishedMap.put(key, draft.getValue());
                    this.draftsMap.remove(key);
                }
            }
        }
        return this.publishedMap.get(key);
    }
}
