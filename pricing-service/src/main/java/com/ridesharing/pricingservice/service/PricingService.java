package com.ridesharing.pricingservice.service;

import com.ridesharing.pricingservice.dto.*;
import com.ridesharing.pricingservice.model.PricingRule;
import com.ridesharing.pricingservice.model.VehicleType;
import com.ridesharing.pricingservice.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRuleRepository pricingRuleRepository;

    public PriceCalculationResponse calculateFare(PriceCalculationRequest request) {
        PricingRule rule = pricingRuleRepository.findByVehicleType(request.getVehicleType())
                .orElseThrow(() -> new RuntimeException(
                        "No pricing rule found for vehicle type: " + request.getVehicleType()));

        double distanceCharge = request.getDistanceInKm() * rule.getPerKmRate();
        double timeCharge = request.getEstimatedDurationMinutes() * rule.getPerMinuteRate();
        double subtotal = rule.getBaseFare() + distanceCharge + timeCharge;

        double surgeCharge = 0.0;
        if (rule.getSurgeMultiplier() > 1.0) {
            surgeCharge = subtotal * (rule.getSurgeMultiplier() - 1.0);
        }

        double totalFare = subtotal + surgeCharge;
        totalFare = Math.max(totalFare, rule.getMinimumFare());
        totalFare = Math.round(totalFare * 100.0) / 100.0;

        String breakdown = String.format(
                "Base: ₹%.2f + Distance(%.1fkm × ₹%.2f): ₹%.2f + Time(%.0fmin × ₹%.2f): ₹%.2f + Surge(%.1fx): ₹%.2f = Total: ₹%.2f",
                rule.getBaseFare(),
                request.getDistanceInKm(), rule.getPerKmRate(), distanceCharge,
                request.getEstimatedDurationMinutes(), rule.getPerMinuteRate(), timeCharge,
                rule.getSurgeMultiplier(), surgeCharge,
                totalFare
        );

        return PriceCalculationResponse.builder()
                .vehicleType(request.getVehicleType())
                .distanceInKm(request.getDistanceInKm())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .baseFare(rule.getBaseFare())
                .distanceCharge(distanceCharge)
                .timeCharge(timeCharge)
                .surgeMultiplier(rule.getSurgeMultiplier())
                .surgeCharge(surgeCharge)
                .totalFare(totalFare)
                .fareBreakdown(breakdown)
                .build();
    }

    public List<PriceEstimateResponse> getAllEstimates(Double distanceInKm, Double durationMinutes) {
        return pricingRuleRepository.findAll().stream()
                .map(rule -> {
                    double fare = rule.getBaseFare()
                            + (distanceInKm * rule.getPerKmRate())
                            + (durationMinutes * rule.getPerMinuteRate());
                    fare *= rule.getSurgeMultiplier();
                    fare = Math.max(fare, rule.getMinimumFare());
                    fare = Math.round(fare * 100.0) / 100.0;

                    return PriceEstimateResponse.builder()
                            .vehicleType(rule.getVehicleType())
                            .estimatedFare(fare)
                            .surgeMultiplier(rule.getSurgeMultiplier())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void updateSurge(VehicleType vehicleType, Double surgeMultiplier) {
        PricingRule rule = pricingRuleRepository.findByVehicleType(vehicleType)
                .orElseThrow(() -> new RuntimeException(
                        "No pricing rule found for vehicle type: " + vehicleType));
        rule.setSurgeMultiplier(surgeMultiplier);
        pricingRuleRepository.save(rule);
    }
}
