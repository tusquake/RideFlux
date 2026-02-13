package com.ridesharing.driverlocationservice.controller;

import com.ridesharing.driverlocationservice.dto.*;
import com.ridesharing.driverlocationservice.service.DriverLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private final DriverLocationService driverLocationService;

    @PostMapping("/update")
    public ResponseEntity<String> updateLocation(@Valid @RequestBody LocationUpdateRequest request) {
        driverLocationService.updateLocation(request);
        return ResponseEntity.ok("Location updated for driver: " + request.getDriverId());
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> findNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusInKm) {
        List<NearbyDriverResponse> drivers = driverLocationService.findNearbyDrivers(latitude, longitude, radiusInKm);
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<DriverLocationResponse> getDriverLocation(@PathVariable Long driverId) {
        DriverLocationResponse response = driverLocationService.getDriverLocation(driverId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/driver/{driverId}")
    public ResponseEntity<String> removeDriver(@PathVariable Long driverId) {
        driverLocationService.removeDriver(driverId);
        return ResponseEntity.ok("Driver " + driverId + " removed from tracking");
    }

    @PostMapping("/simulate")
    public ResponseEntity<String> simulateDrivers() {
        driverLocationService.simulateDrivers();
        return ResponseEntity.ok("10 drivers simulated in Delhi NCR area");
    }
}
