package com.rangeenraat.service;

import com.rangeenraat.dto.*;
import com.rangeenraat.entity.Booking;
import com.rangeenraat.repository.BookingRepository;
import com.razorpay.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {
    private final BookingRepository bookings;

    @Value("${razorpay.key.id}") private String keyId;
    @Value("${razorpay.key.secret}") private String keySecret;
    @Value("${event.name}") private String eventName;

    private static final Map<String, Pass> PASSES = Map.of(
        "solo", new Pass("Solo Pass",499),
        "couple", new Pass("Couple Pass",899),
        "group", new Pass("Group Pass (4)",1699)
    );

    public PaymentService(BookingRepository bookings){this.bookings=bookings;}

    @Transactional
    public Map<String,Object> createOrder(CreateOrderRequest req) throws Exception {
        long rupees=0;
        JSONArray items=new JSONArray();

        for(BookingItemRequest i:req.items()){
            Pass p=PASSES.get(i.passId());
            if(p==null) throw new IllegalArgumentException("Invalid pass: "+i.passId());
            rupees += p.price()*i.quantity();
            items.put(new JSONObject()
                .put("passId",i.passId())
                .put("passName",p.name())
                .put("quantity",i.quantity())
                .put("unitPrice",p.price()));
        }
        if(rupees<=0) throw new IllegalArgumentException("Order amount must be greater than zero");

        String ref="RR-"+UUID.randomUUID().toString().replace("-","").substring(0,10).toUpperCase();

        Booking b=new Booking();
        b.setBookingReference(ref);
        b.setCustomerName(req.name().trim());
        b.setPhone(req.phone().trim());
        b.setEmail(req.email()==null?null:req.email().trim());
        b.setItemsJson(items.toString());
        b.setAmountPaise(rupees*100);
        b.setPaymentStatus("PENDING");
        b.setCreatedAt(LocalDateTime.now());
        bookings.save(b);

        RazorpayClient client=new RazorpayClient(keyId,keySecret);
        JSONObject body=new JSONObject()
            .put("amount",rupees*100)
            .put("currency","INR")
            .put("receipt",ref)
            .put("notes",new JSONObject()
                .put("booking_reference",ref)
                .put("customer_name",req.name()));

        Order order=client.orders.create(body);
        b.setRazorpayOrderId(order.get("id"));
        bookings.save(b);

        return Map.of(
            "bookingId",b.getId(),
            "bookingReference",ref,
            "orderId",order.get("id"),
            "amount",rupees*100,
            "currency","INR",
            "keyId",keyId,
            "eventName",eventName
        );
    }

    @Transactional
    public Map<String,Object> verify(VerifyPaymentRequest req) throws Exception {
        Booking b=bookings.findById(req.bookingIdAsLong())
            .orElseThrow(()->new IllegalArgumentException("Booking not found"));

        if(!req.razorpayOrderId().equals(b.getRazorpayOrderId()))
            throw new IllegalArgumentException("Razorpay order does not match booking");

        JSONObject a=new JSONObject()
            .put("razorpay_order_id",req.razorpayOrderId())
            .put("razorpay_payment_id",req.razorpayPaymentId())
            .put("razorpay_signature",req.razorpaySignature());

        if(!Utils.verifyPaymentSignature(a,keySecret)){
            b.setPaymentStatus("VERIFICATION_FAILED");
            bookings.save(b);
            throw new IllegalArgumentException("Invalid Razorpay signature");
        }

        b.setRazorpayPaymentId(req.razorpayPaymentId());
        b.setPaymentStatus("PAID");
        b.setPaidAt(LocalDateTime.now());
        bookings.save(b);

        return Map.of(
            "success",true,
            "bookingReference",b.getBookingReference(),
            "customerName",b.getCustomerName(),
            "phone",b.getPhone(),
            "paymentStatus","PAID"
        );
    }

    public List<Booking> bookingsForPhone(String phone){
        return bookings.findByPhoneOrderByCreatedAtDesc(phone);
    }

    public Map<String,Object> passes(){
        Map<String,Object> out=new LinkedHashMap<>();
        PASSES.forEach((id,p)->out.put(id,Map.of("id",id,"name",p.name(),"price",p.price())));
        return out;
    }

    private record Pass(String name,long price){}
}
