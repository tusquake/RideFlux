package com.ridesharing.pricingservice.controller;

import com.ridesharing.pricingservice.dto.*;
import com.ridesharing.pricingservice.model.VehicleType;
import com.ridesharing.pricingservice.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/calculate")
    public ResponseEntity<PriceCalculationResponse> calculateFare(
            @Valid @RequestBody PriceCalculationRequest request) {
        PriceCalculationResponse response = pricingService.calculateFare(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estimates")
    public ResponseEntity<List<PriceEstimateResponse>> getAllEstimates(
            @RequestParam Double distanceInKm,
            @RequestParam Double durationMinutes) {
        List<PriceEstimateResponse> estimates = pricingService.getAllEstimates(distanceInKm, durationMinutes);
        return ResponseEntity.ok(estimates);
    }

    @PutMapping("/surge/{vehicleType}")
    public ResponseEntity<String> updateSurge(
            @PathVariable VehicleType vehicleType,
            @RequestParam Double multiplier) {
        pricingService.updateSurge(vehicleType, multiplier);
        return ResponseEntity.ok("Surge updated to " + multiplier + "x for " + vehicleType);
    }
}
