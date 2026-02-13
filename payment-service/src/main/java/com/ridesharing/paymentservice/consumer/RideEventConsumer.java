package com.ridesharing.paymentservice.consumer;

import com.ridesharing.paymentservice.config.RabbitMQConfig;
import com.ridesharing.paymentservice.dto.RideEvent;
import com.ridesharing.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = RabbitMQConfig.RIDE_COMPLETED_QUEUE)
    public void handleRideCompleted(RideEvent event) {
        log.info("Received RIDE_COMPLETED event for ride: {} — processing payment of ₹{}",
                event.getRideId(), event.getFare());
        paymentService.processPayment(event);
    }

    @RabbitListener(queues = RabbitMQConfig.RIDE_CANCELLED_QUEUE)
    public void handleRideCancelled(RideEvent event) {
        log.info("Received RIDE_CANCELLED event for ride: {}", event.getRideId());
        try {
            paymentService.processRefund(event.getRideId());
        } catch (RuntimeException e) {
            log.info("No payment to refund for ride: {} — {}", event.getRideId(), e.getMessage());
        }
    }
}
