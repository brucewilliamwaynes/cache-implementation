package com.cache.dto;

public record CacheEntryResponse(
    String key,
    Object value,
    boolean found
) {}
