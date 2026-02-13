package com.ridesharing.rideservice.event;

import com.ridesharing.rideservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishRideBooked(RideEvent event) {
        event.setEventType("RIDE_BOOKED");
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RIDE_EVENTS_EXCHANGE,
                RabbitMQConfig.RIDE_BOOKED_KEY,
                event
        );
        log.info("Published RIDE_BOOKED event for ride: {}", event.getRideId());
    }

    public void publishRideCompleted(RideEvent event) {
        event.setEventType("RIDE_COMPLETED");
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RIDE_EVENTS_EXCHANGE,
                RabbitMQConfig.RIDE_COMPLETED_KEY,
                event
        );
        log.info("Published RIDE_COMPLETED event for ride: {}", event.getRideId());
    }

    public void publishRideCancelled(RideEvent event) {
        event.setEventType("RIDE_CANCELLED");
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RIDE_EVENTS_EXCHANGE,
                RabbitMQConfig.RIDE_CANCELLED_KEY,
                event
        );
        log.info("Published RIDE_CANCELLED event for ride: {}", event.getRideId());
    }
}
