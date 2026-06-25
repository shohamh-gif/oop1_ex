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

        this.startCleanupThread();
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
                    this.archiveOldValue(key, oldValue);
                }
                this.publishedMap.put(key, draft.getValue());
                this.draftsMap.remove(key);
            }
        }
        return this.publishedMap.get(key);
    }

    private void archiveOldValue(K key, V oldValue) {
        if (!this.historyMap.containsKey(key)) {
            this.historyMap.put(key, new ArrayList<>());
        }
        List<V> historyList = this.historyMap.get(key);
        historyList.add(oldValue);
    }

    public void abort() {
        this.draftsMap.clear();
    }

    private void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    for (List<V> historyList : this.historyMap.values()) {
                        while (historyList.size() > 3) {
                            historyList.removeFirst();
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleaner.start();
    }
}
