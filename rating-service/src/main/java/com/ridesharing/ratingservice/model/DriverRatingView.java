package com.ridesharing.ratingservice.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * CQRS — QUERY MODEL (Read-optimized)
 * 
 * This is the "read side" of CQRS. Instead of calculating AVG() 
 * across millions of rows every time, we maintain a pre-computed
 * summary that gets updated incrementally on every new rating.
 * 
 * Read: O(1) — just fetch this row
 * Write: O(1) — update running average on each new rating
 * 
 * Without CQRS: SELECT AVG(score) FROM ratings WHERE rated_user = ? 
 * → Full table scan, O(n), slow at scale
 */
@Entity
@Table(name = "driver_rating_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRatingView {

    @Id
    private Long driverId;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Long totalRatings = 0L;

    @Builder.Default
    private Long fiveStarCount = 0L;

    @Builder.Default
    private Long fourStarCount = 0L;

    @Builder.Default
    private Long threeStarCount = 0L;

    @Builder.Default
    private Long twoStarCount = 0L;

    @Builder.Default
    private Long oneStarCount = 0L;

    /**
     * Incrementally update average — O(1) math instead of O(n) query
     * new_avg = ((old_avg × old_count) + new_score) / (old_count + 1)
     */
    public void addRating(int score) {
        double totalScore = this.averageRating * this.totalRatings;
        this.totalRatings++;
        this.averageRating = Math.round(((totalScore + score) / this.totalRatings) * 100.0) / 100.0;

        switch (score) {
            case 5 -> this.fiveStarCount++;
            case 4 -> this.fourStarCount++;
            case 3 -> this.threeStarCount++;
            case 2 -> this.twoStarCount++;
            case 1 -> this.oneStarCount++;
        }
    }
}
