package com.ridesharing.pricingservice.config;

import com.ridesharing.pricingservice.model.PricingRule;
import com.ridesharing.pricingservice.model.VehicleType;
import com.ridesharing.pricingservice.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final PricingRuleRepository pricingRuleRepository;

    @Override
    public void run(String... args) {
        if (pricingRuleRepository.count() > 0) {
            log.info("Pricing rules already seeded, skipping...");
            return;
        }

        log.info("Seeding pricing rules...");

        pricingRuleRepository.save(PricingRule.builder()
                .vehicleType(VehicleType.AUTO)
                .baseFare(25.0)
                .perKmRate(8.0)
                .perMinuteRate(1.0)
                .minimumFare(30.0)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .vehicleType(VehicleType.MINI)
                .baseFare(40.0)
                .perKmRate(10.0)
                .perMinuteRate(1.5)
                .minimumFare(50.0)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .vehicleType(VehicleType.SEDAN)
                .baseFare(60.0)
                .perKmRate(14.0)
                .perMinuteRate(2.0)
                .minimumFare(80.0)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .vehicleType(VehicleType.SUV)
                .baseFare(80.0)
                .perKmRate(18.0)
                .perMinuteRate(2.5)
                .minimumFare(100.0)
                .build());

        pricingRuleRepository.save(PricingRule.builder()
                .vehicleType(VehicleType.PREMIUM)
                .baseFare(120.0)
                .perKmRate(25.0)
                .perMinuteRate(3.0)
                .minimumFare(150.0)
                .build());

        log.info("Pricing rules seeded successfully!");
    }
}
