package com.cache.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CacheEntryRequest(
    @NotBlank(message = "Key cannot be blank")
    String key,
    
    @NotNull(message = "Value cannot be null")
    Object value
) {}
