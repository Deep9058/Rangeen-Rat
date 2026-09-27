package com.rangeenraat.controller;

import com.rangeenraat.dto.*;
import com.rangeenraat.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;
import com.rangeenraat.entity.Booking;

@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service){this.service=service;}

    @GetMapping("/passes")
    public Map<String,Object> passes(){return service.passes();}

    /**
     * Phone-number login. The phone number acts as the customer identifier.
     * For production authentication, add OTP verification through an SMS provider.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> request){
        String phone = request.getOrDefault("phone", "").trim();
        if(!Pattern.matches("^[6-9]\\d{9}$", phone)){
            return ResponseEntity.badRequest().body(Map.of("success",false,"message","Enter a valid 10-digit Indian mobile number."));
        }
        return ResponseEntity.ok(Map.of("success",true,"phone",phone,"customerId",phone));
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> bookings(@RequestParam String phone){
        String normalized = phone == null ? "" : phone.trim();
        if(!Pattern.matches("^[6-9]\\d{9}$", normalized)){
            return ResponseEntity.badRequest().body(Map.of("success",false,"message","Invalid phone number."));
        }
        List<Booking> bookings = service.bookingsForPhone(normalized);
        return ResponseEntity.ok(Map.of("success",true,"customerId",normalized,"bookings",bookings));
    }

    @PostMapping("/payment/create-order")
    public ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest request){
        try{return ResponseEntity.ok(service.createOrder(request));}
        catch(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("success",false,"message",e.getMessage()));}
        catch(Exception e){return ResponseEntity.internalServerError().body(Map.of("success",false,"message","Unable to create Razorpay order","detail",String.valueOf(e.getMessage())));}
    }

    @PostMapping("/payment/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyPaymentRequest request){
        try{return ResponseEntity.ok(service.verify(request));}
        catch(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("success",false,"message",e.getMessage()));}
        catch(Exception e){return ResponseEntity.internalServerError().body(Map.of("success",false,"message","Payment verification failed","detail",String.valueOf(e.getMessage())));}
    }
}
