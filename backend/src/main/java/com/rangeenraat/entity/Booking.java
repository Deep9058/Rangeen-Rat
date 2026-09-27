package com.rangeenraat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="bookings")
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=30)
    private String bookingReference;

    @Column(nullable=false, length=100)
    private String customerName;

    @Column(nullable=false, length=15)
    private String phone;

    @Column(length=150)
    private String email;

    @Column(nullable=false, length=2000)
    private String itemsJson;

    @Column(nullable=false)
    private long amountPaise;

    @Column(length=100)
    private String razorpayOrderId;

    @Column(length=100)
    private String razorpayPaymentId;

    @Column(nullable=false, length=30)
    private String paymentStatus;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    public Long getId(){return id;}
    public String getBookingReference(){return bookingReference;}
    public void setBookingReference(String v){bookingReference=v;}
    public String getCustomerName(){return customerName;}
    public void setCustomerName(String v){customerName=v;}
    public String getPhone(){return phone;}
    public void setPhone(String v){phone=v;}
    public String getEmail(){return email;}
    public void setEmail(String v){email=v;}
    public String getItemsJson(){return itemsJson;}
    public void setItemsJson(String v){itemsJson=v;}
    public long getAmountPaise(){return amountPaise;}
    public void setAmountPaise(long v){amountPaise=v;}
    public String getRazorpayOrderId(){return razorpayOrderId;}
    public void setRazorpayOrderId(String v){razorpayOrderId=v;}
    public String getRazorpayPaymentId(){return razorpayPaymentId;}
    public void setRazorpayPaymentId(String v){razorpayPaymentId=v;}
    public String getPaymentStatus(){return paymentStatus;}
    public void setPaymentStatus(String v){paymentStatus=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getPaidAt(){return paidAt;}
    public void setPaidAt(LocalDateTime v){paidAt=v;}
}
