package com.ridesharing.rideservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideEvent implements Serializable {

    private Long rideId;
    private Long riderId;
    private Long driverId;
    private String eventType;
    private String vehicleType;
    private Double fare;
    private String pickupAddress;
    private String dropoffAddress;
    private LocalDateTime timestamp;
}
