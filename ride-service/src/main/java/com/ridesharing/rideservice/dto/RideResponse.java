package com.ridesharing.rideservice.dto;

import com.ridesharing.rideservice.model.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {

    private Long id;
    private Long riderId;
    private Long driverId;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private String pickupAddress;
    private String dropoffAddress;
    private RideStatus status;
    private String vehicleType;
    private Double distanceInKm;
    private Double estimatedDurationMinutes;
    private Double fare;
    private String fareBreakdown;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
