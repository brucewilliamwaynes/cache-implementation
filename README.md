# LRU Cache Implementation

A high-performance, thread-safe **Least Recently Used (LRU) Cache** implementation in Java using Spring Boot.

## Features

- **O(1) Time Complexity** - Constant time for both `get` and `put` operations
- **Thread-Safe** - Uses `ReentrantReadWriteLock` for concurrent access
- **Object Value Support** - Cache values can be any Object type (complex objects, collections, Maps, etc.)
- **Runtime Capacity Configuration** - Dynamically adjust cache capacity via REST API
- **Persistence** - Cache state is persisted to disk and restored on application restart
- **REST API** - Full CRUD operations available via RESTful endpoints
- **Statistics** - Built-in hit/miss tracking and statistics endpoint

## Quick Start

### Prerequisites

- Java 17+
- Gradle 8+ installed (or use an existing Gradle installation)

### Build & Run

```bash
# Build the project
gradle clean build

# Run the application
gradle bootRun
```

The application will start on `http://localhost:8080`.

## REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/cache/{key}` | Get a value by key |
| `POST` | `/api/v1/cache` | Store a key-value pair |
| `DELETE` | `/api/v1/cache/{key}` | Remove a key |
| `DELETE` | `/api/v1/cache` | Clear all entries |
| `GET` | `/api/v1/cache/stats` | Get cache statistics |
| `GET` | `/api/v1/cache/contains/{key}` | Check if key exists |
| `GET` | `/api/v1/cache/capacity` | Get current capacity |
| `PUT` | `/api/v1/cache/capacity` | Update capacity at runtime |

### Example Usage

```bash
# Store a value (supports any JSON object as value)
curl -X POST http://localhost:8080/api/v1/cache \
  -H "Content-Type: application/json" \
  -d '{"key":"user:123","value":{"name":"John","age":30}}'

# Get a value
curl http://localhost:8080/api/v1/cache/user:123

# Get statistics
curl http://localhost:8080/api/v1/cache/stats

# Update capacity at runtime
curl -X PUT http://localhost:8080/api/v1/cache/capacity \
  -H "Content-Type: application/json" \
  -d '{"capacity":200}'
```

## Configuration

Configure the cache via `application.yml`:

```yaml
cache:
  lru:
    capacity: 100              # Initial cache capacity
  persistence:
    enabled: true              # Enable/disable persistence
    file-path: ./cache-data.json  # Persistence file location
```

## Key Design Decisions

| Feature | Decision | Details |
|---------|----------|---------|
| **Value Types** | Any Object | Supports complex objects, collections, Maps via JSON serialization |
| **Runtime Configuration** | Supported | Capacity can be changed at runtime via REST API |
| **Persistence** | Enabled by default | Cache state saved on shutdown, restored on startup |
| **Metrics Integration** | Future Release | Micrometer integration planned for v2.0 |

## Architecture

The implementation uses:
- **HashMap** for O(1) key-value lookups
- **Doubly Linked List** for O(1) access order maintenance
- **ReentrantReadWriteLock** for thread-safe concurrent access
- **Jackson** for JSON serialization (persistence)

## Future Enhancements

- [ ] TTL (Time-To-Live) support for cache entries
- [ ] Micrometer metrics integration (Prometheus/Grafana)
- [ ] Distributed cache support (Redis integration)
- [ ] Additional eviction policies (LFU, FIFO)
- [ ] Periodic auto-save for persistence

## Documentation

See [Implementation Plan](./20260211_Implementation_Plan.md) for detailed technical documentation.

## License

MIT License
