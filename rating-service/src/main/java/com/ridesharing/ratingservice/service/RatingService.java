package com.ridesharing.ratingservice.service;

import com.ridesharing.ratingservice.dto.*;
import com.ridesharing.ratingservice.model.DriverRatingView;
import com.ridesharing.ratingservice.model.Rating;
import com.ridesharing.ratingservice.repository.DriverRatingViewRepository;
import com.ridesharing.ratingservice.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CQRS — Command Query Responsibility Segregation
 * 
 * COMMAND side (writes): saves to `ratings` table (normalized, append-only)
 * QUERY side (reads): reads from `driver_rating_summary` table (denormalized, pre-computed)
 * 
 * Write path: submitRating() → save to ratings + update driver_rating_summary
 * Read path:  getDriverRatingSummary() → read from driver_rating_summary (O(1))
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    // COMMAND side — normalized source of truth
    private final RatingRepository ratingRepository;

    // QUERY side — denormalized read model
    private final DriverRatingViewRepository driverRatingViewRepository;

    /**
     * COMMAND: Write to both models in a single transaction
     * 1. Save rating to command model (ratings table)
     * 2. Update query model (driver_rating_summary table)
     */
    @Transactional
    public RatingResponse submitRating(RatingRequest request) {
        if (ratingRepository.findByRideIdAndRatedBy(request.getRideId(), request.getRatedBy()).isPresent()) {
            throw new RuntimeException("You have already rated this ride");
        }

        // COMMAND: save to normalized ratings table
        Rating rating = Rating.builder()
                .rideId(request.getRideId())
                .ratedBy(request.getRatedBy())
                .ratedUser(request.getRatedUser())
                .score(request.getScore())
                .review(request.getReview())
                .build();

        rating = ratingRepository.save(rating);

        // CQRS: incrementally update the read model (O(1) operation)
        DriverRatingView view = driverRatingViewRepository.findById(request.getRatedUser())
                .orElse(DriverRatingView.builder().driverId(request.getRatedUser()).build());

        view.addRating(request.getScore());
        driverRatingViewRepository.save(view);

        log.info("CQRS: Rating submitted and view updated — Driver {} now has {}/5 avg ({} ratings)",
                request.getRatedUser(), view.getAverageRating(), view.getTotalRatings());
        return mapToResponse(rating);
    }

    /**
     * QUERY: Read from pre-computed summary — O(1) instead of O(n)
     * No AVG() or COUNT() queries needed!
     */
    public DriverRatingSummary getDriverRatingSummary(Long driverId) {
        DriverRatingView view = driverRatingViewRepository.findById(driverId)
                .orElse(DriverRatingView.builder().driverId(driverId).build());

        return DriverRatingSummary.builder()
                .driverId(view.getDriverId())
                .averageRating(view.getAverageRating())
                .totalRatings(view.getTotalRatings())
                .fiveStarCount(view.getFiveStarCount())
                .fourStarCount(view.getFourStarCount())
                .threeStarCount(view.getThreeStarCount())
                .twoStarCount(view.getTwoStarCount())
                .oneStarCount(view.getOneStarCount())
                .build();
    }

    public List<RatingResponse> getRideRatings(Long rideId) {
        return ratingRepository.findByRideId(rideId)
                .stream().map(this::mapToResponse).toList();
    }

    public List<RatingResponse> getUserRatings(Long userId) {
        return ratingRepository.findByRatedUserOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }

    private RatingResponse mapToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .rideId(rating.getRideId())
                .ratedBy(rating.getRatedBy())
                .ratedUser(rating.getRatedUser())
                .score(rating.getScore())
                .review(rating.getReview())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
