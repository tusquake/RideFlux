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
public class PriceEstimateResponse {

    private VehicleType vehicleType;
    private Double estimatedFare;
    private Double surgeMultiplier;
}
