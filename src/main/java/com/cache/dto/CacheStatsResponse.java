package com.cache.dto;

public record CacheStatsResponse(
    long hits,
    long misses,
    double hitRatePercentage,
    int currentSize,
    int maxCapacity
) {}
