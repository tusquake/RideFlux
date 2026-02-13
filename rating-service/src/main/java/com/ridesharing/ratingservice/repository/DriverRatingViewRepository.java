package com.ridesharing.ratingservice.repository;

import com.ridesharing.ratingservice.model.DriverRatingView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CQRS — Query Model Repository (read-optimized)
 * Simple ID lookup — always O(1)
 */
@Repository
public interface DriverRatingViewRepository extends JpaRepository<DriverRatingView, Long> {
}
