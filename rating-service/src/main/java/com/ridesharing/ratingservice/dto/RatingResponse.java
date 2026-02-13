package com.ridesharing.ratingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {

    private Long id;
    private Long rideId;
    private Long ratedBy;
    private Long ratedUser;
    private Integer score;
    private String review;
    private LocalDateTime createdAt;
}
