package com.rangeenraat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookingItemRequest(
    @NotBlank String passId,
    @Min(1) @Max(20) int quantity
) {}
