package com.ridesharing.rideservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideBookingRequest {

    @NotNull(message = "Rider ID is required")
    private Long riderId;

    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;

    @NotNull(message = "Dropoff latitude is required")
    private Double dropoffLatitude;

    @NotNull(message = "Dropoff longitude is required")
    private Double dropoffLongitude;

    private String pickupAddress;

    private String dropoffAddress;

    @NotNull(message = "Vehicle type is required (AUTO, MINI, SEDAN, SUV, PREMIUM)")
    private String vehicleType;
}
