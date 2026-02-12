package com.cache.service;

import java.util.Optional;

public interface CacheService<K, V> {
    Optional<V> get(K key);
    void put(K key, V value);
    boolean remove(K key);
    void clear();
    int size();
    int capacity();
    void setCapacity(int newCapacity);
    boolean containsKey(K key);
    CacheStats getStats();

    /**
     * Basic cache statistics abstraction.
     */
    record CacheStats(
        long hits,
        long misses,
        double hitRatePercentage,
        int currentSize,
        int maxCapacity
    ) {}
}
