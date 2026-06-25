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
    private Map<K, V> backupMap;

    public LaggedMap(int draftSeconds) {
        this.draftSeconds = draftSeconds;
        this.publishedMap = new HashMap<>();
        this.draftsMap = new HashMap<>();
        this.historyMap = new HashMap<>();
        this.backupMap = new HashMap<>();

        this.startManagerThread();
        this.startBackupThread();
    }

    public void put(K key, V value) {
        long currentTime = System.currentTimeMillis();
        DraftEntry<V> newDraft = new DraftEntry<>(value, currentTime);
        this.draftsMap.put(key, newDraft);
    }

    public V get(K key) {
        return this.publishedMap.get(key);
    }

    public void remove(K key, boolean full) {
        long currentTime = System.currentTimeMillis();
        DraftEntry<V> removeDraft = new DraftEntry<>(full, currentTime);
        this.draftsMap.put(key, removeDraft);
    }

    public void abort() {
        this.draftsMap.clear();
    }

    public void rollback() {
        this.publishedMap = new HashMap<>(this.backupMap);
        this.draftsMap.clear();
    }

    private void startManagerThread() {
        Thread managerThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    this.processExpiredDrafts();
                    this.cleanHistory();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        managerThread.start();
    }

    private void processExpiredDrafts() {
        long currentTime = System.currentTimeMillis();
        List<K> keysToProcess = new ArrayList<>();
        for (Map.Entry<K, DraftEntry<V>> entry : this.draftsMap.entrySet()) {
            if (currentTime - entry.getValue().getCurrentTime() >= (this.draftSeconds * 1000L)) {
                keysToProcess.add(entry.getKey());
            }
        }
        for (K key : keysToProcess) {
            DraftEntry<V> draftEntry = this.draftsMap.remove(key);
            this.handleDraft(key, draftEntry);
        }
    }

    private void handleDraft(K key, DraftEntry<V> draftEntry) {
        if (draftEntry.isRemove()) {
            if (draftEntry.isFullRemove()) {
                this.publishedMap.remove(key);
                this.historyMap.remove(key);
            } else {
                List<V> history = this.historyMap.get(key);
                if (history != null && !history.isEmpty()) {
                    this.publishedMap.put(key, history.getLast());
                } else {
                    this.publishedMap.remove(key);
                }
            }
        } else {
            V oldValue = this.publishedMap.get(key);
            if (oldValue != null) {
                this.archiveOldValue(key, oldValue);
            }
            this.publishedMap.put(key, draftEntry.getValue());
        }
    }

    private void cleanHistory() {
        for (List<V> historyList : this.historyMap.values()) {
            while (historyList.size() > 3) {
                historyList.removeFirst();
            }
        }
    }

    private void archiveOldValue(K key, V oldValue) {
        if (!this.historyMap.containsKey(key)) {
            this.historyMap.put(key, new ArrayList<>());
        }
        this.historyMap.get(key).add(oldValue);
    }

    private void startBackupThread() {
        Thread backupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                    this.backupMap = new HashMap<>(this.publishedMap);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        backupThread.start();
    }
}
