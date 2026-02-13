package com.ridesharing.notificationservice.service;

import com.ridesharing.notificationservice.dto.NotificationResponse;
import com.ridesharing.notificationservice.dto.RideEvent;
import com.ridesharing.notificationservice.model.Notification;
import com.ridesharing.notificationservice.model.NotificationType;
import com.ridesharing.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void sendRideBookedNotification(RideEvent event) {
        String riderMsg = String.format("Your %s ride has been booked! Pickup: %s → Drop: %s. Fare: ₹%.2f. Looking for a driver...",
                event.getVehicleType(), event.getPickupAddress(), event.getDropoffAddress(), event.getFare());
        saveNotification(event.getRiderId(), event.getRideId(), NotificationType.RIDE_BOOKED, riderMsg);

        log.info("📱 SMS to Rider {}: {}", event.getRiderId(), riderMsg);
    }

    public void sendRideCompletedNotification(RideEvent event) {
        String riderMsg = String.format("Ride completed! You've reached %s. Amount charged: ₹%.2f. Thank you for riding with us!",
                event.getDropoffAddress(), event.getFare());
        saveNotification(event.getRiderId(), event.getRideId(), NotificationType.RIDE_COMPLETED, riderMsg);

        if (event.getDriverId() != null) {
            String driverMsg = String.format("Ride completed! You earned ₹%.2f for the trip to %s.",
                    event.getFare() * 0.80, event.getDropoffAddress());
            saveNotification(event.getDriverId(), event.getRideId(), NotificationType.RIDE_COMPLETED, driverMsg);
            log.info("📱 SMS to Driver {}: {}", event.getDriverId(), driverMsg);
        }

        log.info("📱 SMS to Rider {}: {}", event.getRiderId(), riderMsg);
    }

    public void sendRideCancelledNotification(RideEvent event) {
        String riderMsg = String.format("Your ride to %s has been cancelled. We hope to serve you again soon!",
                event.getDropoffAddress());
        saveNotification(event.getRiderId(), event.getRideId(), NotificationType.RIDE_CANCELLED, riderMsg);

        if (event.getDriverId() != null) {
            String driverMsg = "A ride has been cancelled. You're now available for new rides.";
            saveNotification(event.getDriverId(), event.getRideId(), NotificationType.RIDE_CANCELLED, driverMsg);
            log.info("📱 SMS to Driver {}: {}", event.getDriverId(), driverMsg);
        }

        log.info("📱 SMS to Rider {}: {}", event.getRiderId(), riderMsg);
    }

    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    private void saveNotification(Long userId, Long rideId, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .rideId(rideId)
                .type(type)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .rideId(notification.getRideId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
