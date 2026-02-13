package com.ridesharing.rideservice.controller;

import com.ridesharing.rideservice.dto.*;
import com.ridesharing.rideservice.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping("/book")
    public ResponseEntity<RideResponse> bookRide(@Valid @RequestBody RideBookingRequest request) {
        RideResponse response = rideService.bookRide(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{rideId}/accept")
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable Long rideId,
            @RequestParam Long driverId) {
        RideResponse response = rideService.acceptRide(rideId, driverId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{rideId}/start")
    public ResponseEntity<RideResponse> startRide(@PathVariable Long rideId) {
        RideResponse response = rideService.startRide(rideId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(@PathVariable Long rideId) {
        RideResponse response = rideService.completeRide(rideId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable Long rideId,
            @RequestParam(defaultValue = "Cancelled by user") String reason) {
        RideResponse response = rideService.cancelRide(rideId, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRide(@PathVariable Long rideId) {
        RideResponse response = rideService.getRide(rideId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<RideResponse>> getRiderHistory(@PathVariable Long riderId) {
        List<RideResponse> history = rideService.getRiderHistory(riderId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideResponse>> getDriverHistory(@PathVariable Long driverId) {
        List<RideResponse> history = rideService.getDriverHistory(driverId);
        return ResponseEntity.ok(history);
    }
}
