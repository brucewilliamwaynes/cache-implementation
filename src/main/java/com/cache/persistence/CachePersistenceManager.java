package com.cache.persistence;

import com.cache.core.Cache;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Manages cache persistence to disk for recovery across application restarts.
 * Uses JSON format for human-readable storage.
 * 
 * @param <K> Key type
 * @param <V> Value type
 */
public class CachePersistenceManager<K, V> {
    
    private static final Logger logger = LoggerFactory.getLogger(CachePersistenceManager.class);
    
    private final String filePath;
    private final ObjectMapper objectMapper;
    
    public CachePersistenceManager(String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Saves the current cache state to disk.
     * @param cache The cache to persist
     */
    public void saveCache(Cache<K, V> cache) {
        try {
            Map<K, V> entries = cache.getAllEntries();
            CacheSnapshot<K, V> snapshot = new CacheSnapshot<>(
                entries,
                cache.capacity(),
                System.currentTimeMillis()
            );
            objectMapper.writeValue(new File(filePath), snapshot);
            logger.info("Cache persisted successfully. Entries: {}", entries.size());
        } catch (IOException e) {
            logger.error("Failed to persist cache to disk: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Loads cache state from disk and populates the cache.
     * @param cache The cache to populate
     */
    @SuppressWarnings("unchecked")
    public void loadCache(Cache<K, V> cache) {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info("No persistence file found at {}. Starting with empty cache.", filePath);
            return;
        }
        
        try {
            CacheSnapshot<K, V> snapshot = objectMapper.readValue(
                file, 
                new TypeReference<CacheSnapshot<K, V>>() {}
            );
            
            // Restore capacity if it was persisted
            if (snapshot.capacity() > 0) {
                cache.setCapacity(snapshot.capacity());
            }
            
            // Restore entries (in reverse order to maintain LRU order)
            Map<K, V> entries = snapshot.entries();
            if (entries != null) {
                // Convert to list and reverse to maintain proper LRU order
                var entryList = new java.util.ArrayList<>(entries.entrySet());
                java.util.Collections.reverse(entryList);
                for (var entry : entryList) {
                    cache.put(entry.getKey(), entry.getValue());
                }
            }
            
            logger.info("Cache loaded from disk. Entries: {}, Timestamp: {}", 
                cache.size(), snapshot.timestamp());
        } catch (IOException e) {
            logger.error("Failed to load cache from disk: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Snapshot record for cache serialization.
     */
    public record CacheSnapshot<K, V>(
        Map<K, V> entries,
        int capacity,
        long timestamp
    ) {}
}
