package com.ridesharing.notificationservice.consumer;

import com.ridesharing.notificationservice.config.RabbitMQConfig;
import com.ridesharing.notificationservice.dto.RideEvent;
import com.ridesharing.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.RIDE_BOOKED_QUEUE)
    public void handleRideBooked(RideEvent event) {
        log.info("Received RIDE_BOOKED event for ride: {}", event.getRideId());
        notificationService.sendRideBookedNotification(event);
    }

    @RabbitListener(queues = RabbitMQConfig.RIDE_COMPLETED_QUEUE)
    public void handleRideCompleted(RideEvent event) {
        log.info("Received RIDE_COMPLETED event for ride: {}", event.getRideId());
        notificationService.sendRideCompletedNotification(event);
    }

    @RabbitListener(queues = RabbitMQConfig.RIDE_CANCELLED_QUEUE)
    public void handleRideCancelled(RideEvent event) {
        log.info("Received RIDE_CANCELLED event for ride: {}", event.getRideId());
        notificationService.sendRideCancelledNotification(event);
    }
}
