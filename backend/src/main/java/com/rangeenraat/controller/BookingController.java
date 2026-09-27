package com.rangeenraat.controller;

import com.rangeenraat.entity.Booking;
import com.rangeenraat.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
public class BookingController {
    private final BookingRepository repository;

    @Value("${admin.api.token:}")
    private String adminToken;

    public BookingController(BookingRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Booking> all(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        authorize(token);
        return repository.findAll();
    }

    @GetMapping("/{reference}")
    public Booking one(@PathVariable String reference,
                       @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        authorize(token);
        return repository.findByBookingReference(reference)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private void authorize(String token) {
        if (adminToken == null || adminToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Admin API is disabled until ADMIN_API_TOKEN is configured");
        }
        if (!adminToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin token");
        }
    }
}
