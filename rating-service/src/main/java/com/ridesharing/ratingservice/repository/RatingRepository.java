package com.ridesharing.ratingservice.repository;

import com.ridesharing.ratingservice.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByRideIdAndRatedBy(Long rideId, Long ratedBy);

    List<Rating> findByRatedUserOrderByCreatedAtDesc(Long ratedUser);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.ratedUser = :userId")
    Double findAverageRatingByRatedUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ratedUser = :userId")
    Long countByRatedUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ratedUser = :userId AND r.score = :score")
    Long countByRatedUserAndScore(@Param("userId") Long userId, @Param("score") Integer score);

    List<Rating> findByRideId(Long rideId);
}
