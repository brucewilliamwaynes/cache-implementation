package com.cache.core;

import java.util.Map;
import java.util.Optional;

/**
 * Generic cache interface defining core cache operations.
 * Supports any Object type as values.
 *
 * @param <K> Key type
 * @param <V> Value type (supports any Object)
 */
public interface Cache<K, V> {

    /**
     * Retrieves a value from the cache.
     *
     * @param key The key to look up
     * @return Optional containing the value if present, otherwise empty
     */
    Optional<V> get(K key);

    /**
     * Stores a key-value pair in the cache.
     * Values can be any Object type including complex objects, collections, etc.
     *
     * @param key   The key
     * @param value The value (any Object type)
     */
    void put(K key, V value);

    /**
     * Removes a key from the cache.
     *
     * @param key The key to remove
     * @return true if the key was present and removed
     */
    boolean remove(K key);

    /**
     * Clears all entries from the cache.
     */
    void clear();

    /**
     * Returns the current number of entries in the cache.
     *
     * @return Current size
     */
    int size();

    /**
     * Returns the maximum capacity of the cache.
     *
     * @return Maximum capacity
     */
    int capacity();

    /**
     * Updates the cache capacity at runtime.
     * If new capacity is smaller than current size, LRU entries will be evicted.
     *
     * @param newCapacity The new maximum capacity
     * @throws IllegalArgumentException if newCapacity is not positive
     */
    void setCapacity(int newCapacity);

    /**
     * Checks if a key exists in the cache.
     *
     * @param key The key to check
     * @return true if the key exists
     */
    boolean containsKey(K key);

    /**
     * Returns all entries in the cache as a Map.
     * Used primarily for persistence operations.
     *
     * @return Map of all key-value pairs in the cache
     */
    Map<K, V> getAllEntries();
}
