package com.ridesharing.pricingservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pricing_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private Double baseFare;

    @Column(nullable = false)
    private Double perKmRate;

    @Column(nullable = false)
    private Double perMinuteRate;

    @Column(nullable = false)
    private Double minimumFare;

    @Column(nullable = false)
    @Builder.Default
    private Double surgeMultiplier = 1.0;
}
