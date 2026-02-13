package com.ridesharing.rideservice.consumer;

import com.ridesharing.rideservice.config.RabbitMQConfig;
import com.ridesharing.rideservice.event.RideEvent;
import com.ridesharing.rideservice.model.Ride;
import com.ridesharing.rideservice.model.RideStatus;
import com.ridesharing.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * SAGA PATTERN — Choreography-based
 * 
 * This consumer listens for payment results and performs
 * COMPENSATING TRANSACTIONS if payment fails.
 * 
 * Flow:
 * 1. Ride completes → publishes RIDE_COMPLETED
 * 2. Payment Service processes payment
 * 3a. Payment SUCCESS → this consumer marks ride as PAID
 * 3b. Payment FAILED → this consumer triggers COMPENSATION
 *     (reverts ride status, marks payment_failed, could trigger refund)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaConsumer {

    private final RideRepository rideRepository;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCESS_QUEUE)
    public void handlePaymentSuccess(RideEvent event) {
        log.info("SAGA Step 3a: Payment SUCCESS for ride: {}", event.getRideId());

        Ride ride = rideRepository.findById(event.getRideId()).orElse(null);
        if (ride == null) {
            log.error("SAGA ERROR: Ride {} not found for payment success event", event.getRideId());
            return;
        }

        ride.setPaymentStatus("PAID");
        rideRepository.save(ride);

        log.info("SAGA COMPLETED ✅ — Ride {} fully completed and paid", event.getRideId());
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(RideEvent event) {
        log.warn("SAGA Step 3b: Payment FAILED for ride: {} — triggering COMPENSATION", event.getRideId());

        Ride ride = rideRepository.findById(event.getRideId()).orElse(null);
        if (ride == null) {
            log.error("SAGA ERROR: Ride {} not found for payment failure event", event.getRideId());
            return;
        }

        // COMPENSATING TRANSACTION — revert or flag the ride
        ride.setPaymentStatus("PAYMENT_FAILED");
        rideRepository.save(ride);

        log.warn("SAGA COMPENSATION ⚠️ — Ride {} marked as PAYMENT_FAILED. Manual intervention required.", event.getRideId());
    }
}
