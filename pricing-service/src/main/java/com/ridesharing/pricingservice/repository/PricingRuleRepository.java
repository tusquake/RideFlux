package com.ridesharing.pricingservice.repository;

import com.ridesharing.pricingservice.model.PricingRule;
import com.ridesharing.pricingservice.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    Optional<PricingRule> findByVehicleType(VehicleType vehicleType);
}
