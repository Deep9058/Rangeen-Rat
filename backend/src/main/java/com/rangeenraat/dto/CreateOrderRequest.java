package com.rangeenraat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank String name,
    @NotBlank @Pattern(regexp="^[6-9]\\d{9}$") String phone,
    @Email String email,
    @NotEmpty List<@Valid BookingItemRequest> items
) {}
