package com.ridesharing.paymentservice.service;

import com.ridesharing.paymentservice.dto.PaymentResponse;
import com.ridesharing.paymentservice.dto.RideEvent;
import com.ridesharing.paymentservice.model.Payment;
import com.ridesharing.paymentservice.model.PaymentMethod;
import com.ridesharing.paymentservice.model.PaymentStatus;
import com.ridesharing.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse processPayment(RideEvent event) {
        if (paymentRepository.findByRideId(event.getRideId()).isPresent()) {
            log.warn("Payment already exists for ride: {}", event.getRideId());
            return mapToResponse(paymentRepository.findByRideId(event.getRideId()).get());
        }

        Payment payment = Payment.builder()
                .rideId(event.getRideId())
                .riderId(event.getRiderId())
                .driverId(event.getDriverId())
                .amount(event.getFare())
                .paymentMethod(PaymentMethod.UPI)
                .status(PaymentStatus.PROCESSING)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment {} created for ride {} — amount: ₹{}", payment.getId(), event.getRideId(), event.getFare());

        // SIMULATE payment processing (in production: call Razorpay/Stripe API)
        try {
            Thread.sleep(500);

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setCompletedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            log.info("Payment {} COMPLETED — txn: {}", payment.getId(), payment.getTransactionId());
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            log.error("Payment {} FAILED: {}", payment.getId(), e.getMessage());
        }

        return mapToResponse(payment);
    }

    public PaymentResponse processRefund(Long rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new RuntimeException("No payment found for ride: " + rideId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Can only refund completed payments. Current status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        log.info("Payment {} REFUNDED for ride {}", payment.getId(), rideId);
        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentByRideId(Long rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new RuntimeException("No payment found for ride: " + rideId));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getRiderPayments(Long riderId) {
        return paymentRepository.findByRiderId(riderId)
                .stream().map(this::mapToResponse).toList();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .rideId(payment.getRideId())
                .riderId(payment.getRiderId())
                .driverId(payment.getDriverId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}
