package com.ridesharing.notificationservice.dto;

import com.ridesharing.notificationservice.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private Long rideId;
    private NotificationType type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
