package com.cache.service;

import com.cache.core.Cache;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LRUCacheService implements CacheService<String, Object> {

    private final Cache<String, Object> cache;
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);

    public LRUCacheService(Cache<String, Object> cache) {
        this.cache = cache;
    }

    @Override
    public Optional<Object> get(String key) {
        Optional<Object> result = cache.get(key);
        if (result.isPresent()) {
            hitCount.incrementAndGet();
        } else {
            missCount.incrementAndGet();
        }
        return result;
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public boolean remove(String key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
        hitCount.set(0);
        missCount.set(0);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public int capacity() {
        return cache.capacity();
    }

    @Override
    public void setCapacity(int newCapacity) {
        cache.setCapacity(newCapacity);
    }

    @Override
    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    @Override
    public CacheStats getStats() {
        long hits = hitCount.get();
        long misses = missCount.get();
        double hitRate = (hits + misses) > 0
            ? (double) hits / (hits + misses) * 100
            : 0.0;
        return new CacheStats(hits, misses, hitRate, size(), capacity());
    }
}
