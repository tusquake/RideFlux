package com.ridesharing.pricingservice.dto;

import com.ridesharing.pricingservice.model.VehicleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequest {

    @NotNull(message = "Distance in km is required")
    @Positive(message = "Distance must be positive")
    private Double distanceInKm;

    @NotNull(message = "Estimated duration in minutes is required")
    @Positive(message = "Duration must be positive")
    private Double estimatedDurationMinutes;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
}
