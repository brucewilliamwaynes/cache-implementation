package com.cache.dto;

import jakarta.validation.constraints.Min;

public record CapacityUpdateRequest(
    @Min(value = 1, message = "Capacity must be at least 1")
    int capacity
) {}
