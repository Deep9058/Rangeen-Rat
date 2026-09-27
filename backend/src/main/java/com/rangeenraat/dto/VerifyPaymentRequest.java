package com.rangeenraat.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentRequest(
    @NotBlank String bookingId,
    @NotBlank String razorpayOrderId,
    @NotBlank String razorpayPaymentId,
    @NotBlank String razorpaySignature
) {
    public Long bookingIdAsLong() {
        return Long.valueOf(bookingId);
    }
}
