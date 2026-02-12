package com.cache.config;

import com.cache.core.Cache;
import com.cache.core.LRUCache;
import com.cache.persistence.CachePersistenceManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Configuration
public class CacheConfig {

    @Value("${cache.lru.capacity:100}")
    private int initialCacheCapacity;

    @Value("${cache.persistence.enabled:true}")
    private boolean persistenceEnabled;

    @Value("${cache.persistence.file-path:./cache-data.json}")
    private String persistenceFilePath;

    private LRUCache<String, Object> lruCacheInstance;
    private CachePersistenceManager<String, Object> persistenceManager;

    @Bean
    public Cache<String, Object> lruCache() {
        lruCacheInstance = new LRUCache<>(initialCacheCapacity);
        return lruCacheInstance;
    }

    @Bean
    public CachePersistenceManager<String, Object> cachePersistenceManager() {
        persistenceManager = new CachePersistenceManager<>(persistenceFilePath);
        return persistenceManager;
    }

    @PostConstruct
    public void loadCacheFromDisk() {
        if (persistenceEnabled && lruCacheInstance != null && persistenceManager != null) {
            persistenceManager.loadCache(lruCacheInstance);
        }
    }

    @PreDestroy
    public void saveCacheToDisk() {
        if (persistenceEnabled && lruCacheInstance != null && persistenceManager != null) {
            persistenceManager.saveCache(lruCacheInstance);
        }
    }
}
