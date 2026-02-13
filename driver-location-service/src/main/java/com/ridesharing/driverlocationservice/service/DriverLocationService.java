package com.ridesharing.driverlocationservice.service;

import com.ridesharing.driverlocationservice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverLocationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String DRIVER_LOCATIONS_KEY = "driver:locations";
    private static final String DRIVER_AVAILABILITY_KEY = "driver:availability:";

    public void updateLocation(LocationUpdateRequest request) {
        redisTemplate.opsForGeo().add(
                DRIVER_LOCATIONS_KEY,
                new Point(request.getLongitude(), request.getLatitude()),
                request.getDriverId().toString()
        );

        redisTemplate.opsForValue().set(
                DRIVER_AVAILABILITY_KEY + request.getDriverId(),
                request.getIsAvailable().toString()
        );

        log.info("Updated location for driver {}: lat={}, lng={}, available={}",
                request.getDriverId(), request.getLatitude(), request.getLongitude(), request.getIsAvailable());
    }

    public List<NearbyDriverResponse> findNearbyDrivers(Double latitude, Double longitude, Double radiusInKm) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(
                DRIVER_LOCATIONS_KEY,
                new Circle(
                        new Point(longitude, latitude),
                        new Distance(radiusInKm, Metrics.KILOMETERS)
                ),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                        .includeCoordinates()
                        .includeDistance()
                        .sortAscending()
                        .limit(20)
        );

        List<NearbyDriverResponse> nearbyDrivers = new ArrayList<>();

        if (results != null) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                String driverId = result.getContent().getName();

                String availability = redisTemplate.opsForValue().get(DRIVER_AVAILABILITY_KEY + driverId);
                if (!"true".equals(availability)) {
                    continue;
                }

                Point point = result.getContent().getPoint();
                nearbyDrivers.add(NearbyDriverResponse.builder()
                        .driverId(Long.parseLong(driverId))
                        .latitude(point.getY())
                        .longitude(point.getX())
                        .distanceInKm(Math.round(result.getDistance().getValue() * 100.0) / 100.0)
                        .build());
            }
        }

        log.info("Found {} available drivers near lat={}, lng={} within {}km",
                nearbyDrivers.size(), latitude, longitude, radiusInKm);
        return nearbyDrivers;
    }

    public DriverLocationResponse getDriverLocation(Long driverId) {
        List<Point> positions = redisTemplate.opsForGeo().position(
                DRIVER_LOCATIONS_KEY,
                driverId.toString()
        );

        if (positions == null || positions.isEmpty() || positions.get(0) == null) {
            throw new RuntimeException("Location not found for driver: " + driverId);
        }

        Point point = positions.get(0);
        String availability = redisTemplate.opsForValue().get(DRIVER_AVAILABILITY_KEY + driverId);

        return DriverLocationResponse.builder()
                .driverId(driverId)
                .latitude(point.getY())
                .longitude(point.getX())
                .isAvailable("true".equals(availability))
                .build();
    }

    public void removeDriver(Long driverId) {
        redisTemplate.opsForGeo().remove(DRIVER_LOCATIONS_KEY, driverId.toString());
        redisTemplate.delete(DRIVER_AVAILABILITY_KEY + driverId);
        log.info("Removed driver {} from location tracking", driverId);
    }

    public void simulateDrivers() {
        double[][] delhiLocations = {
                {28.6139, 77.2090},
                {28.6280, 77.2190},
                {28.6350, 77.2250},
                {28.6450, 77.2100},
                {28.5950, 77.2300},
                {28.6100, 77.2400},
                {28.6200, 77.2000},
                {28.6500, 77.2350},
                {28.5800, 77.2150},
                {28.6050, 77.1950}
        };

        for (int i = 0; i < delhiLocations.length; i++) {
            long driverId = 100 + i;
            updateLocation(LocationUpdateRequest.builder()
                    .driverId(driverId)
                    .latitude(delhiLocations[i][0])
                    .longitude(delhiLocations[i][1])
                    .isAvailable(true)
                    .build());
        }

        log.info("Simulated {} drivers in Delhi NCR area", delhiLocations.length);
    }
}
