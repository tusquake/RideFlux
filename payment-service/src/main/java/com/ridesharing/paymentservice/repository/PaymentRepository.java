package com.ridesharing.paymentservice.repository;

import com.ridesharing.paymentservice.model.Payment;
import com.ridesharing.paymentservice.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRideId(Long rideId);

    List<Payment> findByRiderId(Long riderId);

    List<Payment> findByStatus(PaymentStatus status);
}
