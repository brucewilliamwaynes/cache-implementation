# LRU Cache Implementation Plan

## Project Overview

This document outlines a comprehensive plan to implement a **Least Recently Used (LRU) Cache** in Java using the Spring Framework, packaged as a Maven project.

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Project Structure](#2-project-structure)
3. [Maven Configuration](#3-maven-configuration)
4. [Core Implementation](#4-core-implementation)
5. [Spring Integration](#5-spring-integration)
6. [REST API Layer](#6-rest-api-layer)
7. [Testing Strategy](#7-testing-strategy)
8. [Success Criteria](#8-success-criteria)
9. [Future Enhancements](#9-future-enhancements)

---

## 1. Introduction

### 1.1 What is LRU Cache?

An **LRU (Least Recently Used) Cache** is a data structure that:
- Stores a limited number of items
- Evicts the **least recently used** item when the cache reaches its capacity
- Provides **O(1)** time complexity for both `get` and `put` operations

### 1.2 Use Cases

- Database query result caching
- Session management
- API response caching
- Resource pooling

### 1.3 Technical Approach

The implementation will use a combination of:
- **HashMap** for O(1) key-value lookups
- **Doubly Linked List** for O(1) insertion/deletion and maintaining access order

---

## 2. Project Structure

```
cache-implementation/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── cache/
│   │   │           ├── CacheApplication.java
│   │   │           ├── config/
│   │   │           │   └── CacheConfig.java
│   │   │           ├── core/
│   │   │           │   ├── Cache.java                    # Interface
│   │   │           │   ├── LRUCache.java                 # Core implementation
│   │   │           │   └── Node.java                     # Doubly linked list node
│   │   │           ├── service/
│   │   │           │   ├── CacheService.java             # Service interface
│   │   │           │   └── LRUCacheService.java          # Service implementation
│   │   │           ├── controller/
│   │   │           │   └── CacheController.java          # REST endpoints
│   │   │           ├── dto/
│   │   │           │   ├── CacheEntryRequest.java
│   │   │           │   ├── CacheEntryResponse.java
│   │   │           │   └── CacheStatsResponse.java
│   │   │           └── exception/
│   │   │               ├── CacheException.java
│   │   │               └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── cache/
│                   ├── core/
│                   │   └── LRUCacheTest.java
│                   ├── service/
│                   │   └── LRUCacheServiceTest.java
│                   └── controller/
│                       └── CacheControllerTest.java
└── README.md
```

---

## 3. Maven Configuration

### 3.1 Create `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>

    <groupId>com.cache</groupId>
    <artifactId>lru-cache-implementation</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>LRU Cache Implementation</name>
    <description>LRU Cache implementation using Java and Spring Boot</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- Spring Boot Starter Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Starter Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring Boot Actuator (for metrics) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Lombok (optional - for reducing boilerplate) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.2 Dependencies Rationale

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST API support |
| `spring-boot-starter-validation` | Input validation |
| `spring-boot-starter-actuator` | Health checks and metrics |
| `lombok` | Reduce boilerplate code |
| `spring-boot-starter-test` | Unit and integration testing |

---

## 4. Core Implementation

### 4.1 Phase 1: Node Class (Doubly Linked List Node)

**File:** `src/main/java/com/cache/core/Node.java`

```java
package com.cache.core;

/**
 * Represents a node in the doubly linked list used by LRU Cache.
 * @param <K> Key type
 * @param <V> Value type
 */
public class Node<K, V> {
    K key;
    V value;
    Node<K, V> prev;
    Node<K, V> next;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
```

**Key Points:**
- Generic types for flexibility
- Package-private access for encapsulation
- Simple POJO structure

---

### 4.2 Phase 2: Cache Interface

**File:** `src/main/java/com/cache/core/Cache.java`

```java
package com.cache.core;

import java.util.Optional;

/**
 * Generic cache interface defining core cache operations.
 * @param <K> Key type
 * @param <V> Value type
 */
public interface Cache<K, V> {
    
    /**
     * Retrieves a value from the cache.
     * @param key The key to look up
     * @return Optional containing the value if present
     */
    Optional<V> get(K key);
    
    /**
     * Stores a key-value pair in the cache.
     * @param key The key
     * @param value The value
     */
    void put(K key, V value);
    
    /**
     * Removes a key from the cache.
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
     * @return Current size
     */
    int size();
    
    /**
     * Returns the maximum capacity of the cache.
     * @return Maximum capacity
     */
    int capacity();
    
    /**
     * Checks if a key exists in the cache.
     * @param key The key to check
     * @return true if the key exists
     */
    boolean containsKey(K key);
}
```

---

### 4.3 Phase 3: LRU Cache Implementation

**File:** `src/main/java/com/cache/core/LRUCache.java`

```java
package com.cache.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe LRU Cache implementation using HashMap and Doubly Linked List.
 * Provides O(1) time complexity for get and put operations.
 * 
 * @param <K> Key type
 * @param <V> Value type
 */
public class LRUCache<K, V> implements Cache<K, V> {
    
    private final int capacity;
    private final Map<K, Node<K, V>> cache;
    private final Node<K, V> head;  // Dummy head (most recently used)
    private final Node<K, V> tail;  // Dummy tail (least recently used)
    private final ReentrantReadWriteLock lock;
    
    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
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
                if (cache.size() > capacity) {
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
        return capacity;
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
```

**Implementation Details:**

| Component | Purpose |
|-----------|---------|
| `HashMap` | O(1) key lookup |
| `Doubly Linked List` | O(1) reordering of access order |
| `Dummy Head/Tail` | Simplifies edge case handling |
| `ReentrantReadWriteLock` | Thread-safety with read/write separation |

---

## 5. Spring Integration

### 5.1 Phase 4: Spring Boot Application

**File:** `src/main/java/com/cache/CacheApplication.java`

```java
package com.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CacheApplication {
    public static void main(String[] args) {
        SpringApplication.run(CacheApplication.class, args);
    }
}
```

---

### 5.2 Phase 5: Configuration

**File:** `src/main/java/com/cache/config/CacheConfig.java`

```java
package com.cache.config;

import com.cache.core.Cache;
import com.cache.core.LRUCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    
    @Value("${cache.lru.capacity:100}")
    private int cacheCapacity;
    
    @Bean
    public Cache<String, Object> lruCache() {
        return new LRUCache<>(cacheCapacity);
    }
}
```

**File:** `src/main/resources/application.yml`

```yaml
server:
  port: 8080

cache:
  lru:
    capacity: 100

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

### 5.3 Phase 6: Service Layer

**File:** `src/main/java/com/cache/service/CacheService.java`

```java
package com.cache.service;

import java.util.Optional;

public interface CacheService<K, V> {
    Optional<V> get(K key);
    void put(K key, V value);
    boolean remove(K key);
    void clear();
    int size();
    int capacity();
    boolean containsKey(K key);
    CacheStats getStats();
}
```

**File:** `src/main/java/com/cache/service/LRUCacheService.java`

```java
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
    
    public record CacheStats(
        long hits,
        long misses,
        double hitRatePercentage,
        int currentSize,
        int maxCapacity
    ) {}
}
```

---

## 6. REST API Layer

### 6.1 Phase 7: DTOs

**File:** `src/main/java/com/cache/dto/CacheEntryRequest.java`

```java
package com.cache.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CacheEntryRequest(
    @NotBlank(message = "Key cannot be blank")
    String key,
    
    @NotNull(message = "Value cannot be null")
    Object value
) {}
```

**File:** `src/main/java/com/cache/dto/CacheEntryResponse.java`

```java
package com.cache.dto;

public record CacheEntryResponse(
    String key,
    Object value,
    boolean found
) {}
```

**File:** `src/main/java/com/cache/dto/CacheStatsResponse.java`

```java
package com.cache.dto;

public record CacheStatsResponse(
    long hits,
    long misses,
    double hitRatePercentage,
    int currentSize,
    int maxCapacity
) {}
```

---

### 6.2 Phase 8: REST Controller

**File:** `src/main/java/com/cache/controller/CacheController.java`

```java
package com.cache.controller;

import com.cache.dto.CacheEntryRequest;
import com.cache.dto.CacheEntryResponse;
import com.cache.dto.CacheStatsResponse;
import com.cache.service.CacheService;
import com.cache.service.LRUCacheService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {
    
    private final CacheService<String, Object> cacheService;
    
    public CacheController(CacheService<String, Object> cacheService) {
        this.cacheService = cacheService;
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<CacheEntryResponse> get(@PathVariable String key) {
        return cacheService.get(key)
            .map(value -> ResponseEntity.ok(new CacheEntryResponse(key, value, true)))
            .orElse(ResponseEntity.ok(new CacheEntryResponse(key, null, false)));
    }
    
    @PostMapping
    public ResponseEntity<Void> put(@Valid @RequestBody CacheEntryRequest request) {
        cacheService.put(request.key(), request.value());
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> remove(@PathVariable String key) {
        boolean removed = cacheService.remove(key);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @DeleteMapping
    public ResponseEntity<Void> clear() {
        cacheService.clear();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats")
    public ResponseEntity<CacheStatsResponse> getStats() {
        LRUCacheService.CacheStats stats = 
            ((LRUCacheService) cacheService).getStats();
        return ResponseEntity.ok(new CacheStatsResponse(
            stats.hits(),
            stats.misses(),
            stats.hitRatePercentage(),
            stats.currentSize(),
            stats.maxCapacity()
        ));
    }
    
    @GetMapping("/contains/{key}")
    public ResponseEntity<Boolean> containsKey(@PathVariable String key) {
        return ResponseEntity.ok(cacheService.containsKey(key));
    }
}
```

---

### 6.3 Phase 9: Exception Handling

**File:** `src/main/java/com/cache/exception/CacheException.java`

```java
package com.cache.exception;

public class CacheException extends RuntimeException {
    public CacheException(String message) {
        super(message);
    }
    
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**File:** `src/main/java/com/cache/exception/GlobalExceptionHandler.java`

```java
package com.cache.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CacheException.class)
    public ResponseEntity<Map<String, Object>> handleCacheException(CacheException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
```

---

## 7. Testing Strategy

### 7.1 Phase 10: Unit Tests for Core LRU Cache

**File:** `src/test/java/com/cache/core/LRUCacheTest.java`

```java
package com.cache.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {
    
    private LRUCache<String, Integer> cache;
    
    @BeforeEach
    void setUp() {
        cache = new LRUCache<>(3);
    }
    
    @Test
    @DisplayName("Should store and retrieve values")
    void testPutAndGet() {
        cache.put("a", 1);
        cache.put("b", 2);
        
        assertEquals(Optional.of(1), cache.get("a"));
        assertEquals(Optional.of(2), cache.get("b"));
    }
    
    @Test
    @DisplayName("Should return empty for non-existent keys")
    void testGetNonExistent() {
        assertEquals(Optional.empty(), cache.get("nonexistent"));
    }
    
    @Test
    @DisplayName("Should evict LRU item when capacity exceeded")
    void testEviction() {
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        cache.put("d", 4); // Should evict "a"
        
        assertEquals(Optional.empty(), cache.get("a"));
        assertEquals(Optional.of(2), cache.get("b"));
        assertEquals(Optional.of(3), cache.get("c"));
        assertEquals(Optional.of(4), cache.get("d"));
    }
    
    @Test
    @DisplayName("Should update access order on get")
    void testAccessOrderOnGet() {
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        
        cache.get("a"); // Access "a", making it most recently used
        cache.put("d", 4); // Should evict "b" (now LRU)
        
        assertEquals(Optional.of(1), cache.get("a"));
        assertEquals(Optional.empty(), cache.get("b"));
    }
    
    @Test
    @DisplayName("Should update existing key value")
    void testUpdateExistingKey() {
        cache.put("a", 1);
        cache.put("a", 10);
        
        assertEquals(Optional.of(10), cache.get("a"));
        assertEquals(1, cache.size());
    }
    
    @Test
    @DisplayName("Should remove key correctly")
    void testRemove() {
        cache.put("a", 1);
        cache.put("b", 2);
        
        assertTrue(cache.remove("a"));
        assertFalse(cache.remove("nonexistent"));
        assertEquals(Optional.empty(), cache.get("a"));
        assertEquals(1, cache.size());
    }
    
    @Test
    @DisplayName("Should clear all entries")
    void testClear() {
        cache.put("a", 1);
        cache.put("b", 2);
        cache.clear();
        
        assertEquals(0, cache.size());
        assertEquals(Optional.empty(), cache.get("a"));
    }
    
    @Test
    @DisplayName("Should throw exception for invalid capacity")
    void testInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(0));
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(-1));
    }
}
```

---

### 7.2 Phase 11: Integration Tests

**File:** `src/test/java/com/cache/controller/CacheControllerTest.java`

```java
package com.cache.controller;

import com.cache.dto.CacheEntryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CacheControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldPutAndGetValue() throws Exception {
        CacheEntryRequest request = new CacheEntryRequest("testKey", "testValue");
        
        // Put value
        mockMvc.perform(post("/api/v1/cache")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
        
        // Get value
        mockMvc.perform(get("/api/v1/cache/testKey"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value("testKey"))
            .andExpect(jsonPath("$.value").value("testValue"))
            .andExpect(jsonPath("$.found").value(true));
    }
    
    @Test
    void shouldReturnNotFoundForMissingKey() throws Exception {
        mockMvc.perform(get("/api/v1/cache/nonexistent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.found").value(false));
    }
    
    @Test
    void shouldReturnStats() throws Exception {
        mockMvc.perform(get("/api/v1/cache/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.maxCapacity").exists())
            .andExpect(jsonPath("$.currentSize").exists());
    }
}
```

---

## 8. Success Criteria

### 8.1 Functional Requirements

| Requirement | Acceptance Criteria |
|-------------|---------------------|
| **O(1) Get Operation** | Get operation completes in constant time |
| **O(1) Put Operation** | Put operation completes in constant time |
| **LRU Eviction** | Least recently used item is evicted when capacity is exceeded |
| **Thread Safety** | Concurrent access does not cause data corruption |
| **REST API** | All CRUD operations available via REST endpoints |

### 8.2 Non-Functional Requirements

| Requirement | Acceptance Criteria |
|-------------|---------------------|
| **Test Coverage** | Minimum 80% code coverage |
| **Documentation** | All public methods have Javadoc |
| **Build** | Maven build completes without errors |
| **Performance** | Handles 10,000 operations/second |

### 8.3 Verification Commands

```bash
# Build the project
mvn clean install

# Run tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Start the application
mvn spring-boot:run

# Test API endpoints
curl -X POST http://localhost:8080/api/v1/cache \
  -H "Content-Type: application/json" \
  -d '{"key":"test","value":"hello"}'

curl http://localhost:8080/api/v1/cache/test

curl http://localhost:8080/api/v1/cache/stats
```

---

## 9. Future Enhancements

### 9.1 Potential Improvements

1. **TTL (Time-To-Live) Support**
   - Add expiration time for cache entries
   - Background thread for cleanup

2. **Distributed Cache**
   - Redis integration for distributed caching
   - Cluster support

3. **Metrics & Monitoring**
   - Micrometer integration
   - Prometheus/Grafana dashboards

4. **Additional Eviction Policies**
   - LFU (Least Frequently Used)
   - FIFO (First In First Out)
   - Random eviction

5. **Persistence**
   - Disk-based backup
   - Recovery on restart

---

## Implementation Timeline

| Phase | Task | Estimated Time |
|-------|------|----------------|
| 1-3 | Core LRU Cache Implementation | 2-3 hours |
| 4-6 | Spring Integration & Service Layer | 1-2 hours |
| 7-9 | REST API & Exception Handling | 1-2 hours |
| 10-11 | Testing | 2-3 hours |
| - | Documentation & Review | 1 hour |
| **Total** | | **7-11 hours** |

---

## Open Questions

1. **Serialization**: Should the cache support serializable values only, or any object type?
2. **Metrics**: Should we integrate with Micrometer for production-grade metrics?
3. **Configuration**: Should capacity be configurable at runtime or only at startup?
4. **Persistence**: Is there a need for cache persistence across restarts?

---

*Document Version: 1.0*  
*Created: 2026-02-11*  
*Author: Implementation Planning Assistant*
