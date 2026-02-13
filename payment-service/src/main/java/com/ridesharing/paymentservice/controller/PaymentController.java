package com.ridesharing.paymentservice.controller;

import com.ridesharing.paymentservice.dto.PaymentResponse;
import com.ridesharing.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentByRideId(@PathVariable Long rideId) {
        PaymentResponse response = paymentService.getPaymentByRideId(rideId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<PaymentResponse>> getRiderPayments(@PathVariable Long riderId) {
        List<PaymentResponse> payments = paymentService.getRiderPayments(riderId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/refund/{rideId}")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long rideId) {
        PaymentResponse response = paymentService.processRefund(rideId);
        return ResponseEntity.ok(response);
    }
}
