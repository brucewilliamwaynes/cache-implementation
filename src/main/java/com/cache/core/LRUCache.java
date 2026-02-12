package com.cache.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe LRU Cache implementation using HashMap and Doubly Linked List.
 * Provides O(1) time complexity for get and put operations.
 * Supports runtime capacity configuration and any Object type as values.
 *
 * @param <K> Key type
 * @param <V> Value type (supports any Object)
 */
public class LRUCache<K, V> implements Cache<K, V> {

    private final AtomicInteger capacity;
    private final Map<K, Node<K, V>> cache;
    private final Node<K, V> head;  // Dummy head (most recently used)
    private final Node<K, V> tail;  // Dummy tail (least recently used)
    private final ReentrantReadWriteLock lock;

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = new AtomicInteger(capacity);
        this.cache = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();

        // Initialize dummy head and tail
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public Optional<V> get(K key) {
        lock.readLock().lock();
        try {
            Node<K, V> node = cache.get(key);
            if (node == null) {
                return Optional.empty();
            }
            // Move to front (most recently used)
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                moveToHead(node);
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
            return Optional.of(node.value);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            Node<K, V> node = cache.get(key);

            if (node != null) {
                // Update existing node
                node.value = value;
                moveToHead(node);
            } else {
                // Create new node
                Node<K, V> newNode = new Node<>(key, value);
                cache.put(key, newNode);
                addToHead(newNode);

                // Evict if over capacity
                while (cache.size() > capacity.get()) {
                    Node<K, V> lru = removeTail();
                    cache.remove(lru.key);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(K key) {
        lock.writeLock().lock();
        try {
            Node<K, V> node = cache.remove(key);
            if (node != null) {
                removeNode(node);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            head.next = tail;
            tail.prev = head;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int capacity() {
        return capacity.get();
    }

    @Override
    public void setCapacity(int newCapacity) {
        if (newCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        lock.writeLock().lock();
        try {
            this.capacity.set(newCapacity);
            // Evict LRU entries if current size exceeds new capacity
            while (cache.size() > newCapacity) {
                Node<K, V> lru = removeTail();
                cache.remove(lru.key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        lock.readLock().lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Map<K, V> getAllEntries() {
        lock.readLock().lock();
        try {
            // Return entries in LRU order (most recently used first)
            Map<K, V> entries = new LinkedHashMap<>();
            Node<K, V> current = head.next;
            while (current != tail) {
                entries.put(current.key, current.value);
                current = current.next;
            }
            return entries;
        } finally {
            lock.readLock().unlock();
        }
    }

    // ============ Private Helper Methods ============

    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    private Node<K, V> removeTail() {
        Node<K, V> lru = tail.prev;
        removeNode(lru);
        return lru;
    }
}
