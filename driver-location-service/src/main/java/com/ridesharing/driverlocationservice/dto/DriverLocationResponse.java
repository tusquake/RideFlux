package com.ridesharing.driverlocationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationResponse {

    private Long driverId;
    private Double latitude;
    private Double longitude;
    private Boolean isAvailable;
}
