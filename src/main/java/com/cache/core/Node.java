package com.cache.core;

/**
 * Represents a node in the doubly linked list used by LRU Cache.
 * Each node stores a key-value pair and maintains references to
 * its previous and next nodes in the list.
 * 
 * @param <K> Key type
 * @param <V> Value type
 */
public class Node<K, V> {
    K key;
    V value;
    Node<K, V> prev;
    Node<K, V> next;

    /**
     * Creates a new node with the specified key and value.
     * 
     * @param key   The key associated with this node
     * @param value The value stored in this node
     */
    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
