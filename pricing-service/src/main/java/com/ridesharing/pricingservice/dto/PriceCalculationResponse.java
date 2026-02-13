package com.ridesharing.pricingservice.dto;

import com.ridesharing.pricingservice.model.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationResponse {

    private VehicleType vehicleType;
    private Double distanceInKm;
    private Double estimatedDurationMinutes;
    private Double baseFare;
    private Double distanceCharge;
    private Double timeCharge;
    private Double surgeMultiplier;
    private Double surgeCharge;
    private Double totalFare;
    private String fareBreakdown;
}
