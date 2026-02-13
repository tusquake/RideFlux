package com.ridesharing.ratingservice.service;

import com.ridesharing.ratingservice.dto.*;
import com.ridesharing.ratingservice.model.Rating;
import com.ridesharing.ratingservice.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingResponse submitRating(RatingRequest request) {
        if (ratingRepository.findByRideIdAndRatedBy(request.getRideId(), request.getRatedBy()).isPresent()) {
            throw new RuntimeException("You have already rated this ride");
        }

        Rating rating = Rating.builder()
                .rideId(request.getRideId())
                .ratedBy(request.getRatedBy())
                .ratedUser(request.getRatedUser())
                .score(request.getScore())
                .review(request.getReview())
                .build();

        rating = ratingRepository.save(rating);

        log.info("Rating submitted: Ride {} — {} gave {} a {}/5 star rating",
                request.getRideId(), request.getRatedBy(), request.getRatedUser(), request.getScore());
        return mapToResponse(rating);
    }

    public DriverRatingSummary getDriverRatingSummary(Long driverId) {
        Double avgRating = ratingRepository.findAverageRatingByRatedUser(driverId);
        Long totalRatings = ratingRepository.countByRatedUser(driverId);

        return DriverRatingSummary.builder()
                .driverId(driverId)
                .averageRating(avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0)
                .totalRatings(totalRatings)
                .fiveStarCount(ratingRepository.countByRatedUserAndScore(driverId, 5))
                .fourStarCount(ratingRepository.countByRatedUserAndScore(driverId, 4))
                .threeStarCount(ratingRepository.countByRatedUserAndScore(driverId, 3))
                .twoStarCount(ratingRepository.countByRatedUserAndScore(driverId, 2))
                .oneStarCount(ratingRepository.countByRatedUserAndScore(driverId, 1))
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
