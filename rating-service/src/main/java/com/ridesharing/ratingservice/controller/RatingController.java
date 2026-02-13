package com.ridesharing.ratingservice.controller;

import com.ridesharing.ratingservice.dto.*;
import com.ridesharing.ratingservice.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponse> submitRating(@Valid @RequestBody RatingRequest request) {
        RatingResponse response = ratingService.submitRating(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/{driverId}/summary")
    public ResponseEntity<DriverRatingSummary> getDriverSummary(@PathVariable Long driverId) {
        DriverRatingSummary summary = ratingService.getDriverRatingSummary(driverId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<RatingResponse>> getRideRatings(@PathVariable Long rideId) {
        List<RatingResponse> ratings = ratingService.getRideRatings(rideId);
        return ResponseEntity.ok(ratings);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingResponse>> getUserRatings(@PathVariable Long userId) {
        List<RatingResponse> ratings = ratingService.getUserRatings(userId);
        return ResponseEntity.ok(ratings);
    }
}
