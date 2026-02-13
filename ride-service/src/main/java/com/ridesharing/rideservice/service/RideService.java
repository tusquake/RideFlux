package com.ridesharing.rideservice.service;

import com.ridesharing.rideservice.dto.*;
import com.ridesharing.rideservice.event.RideEvent;
import com.ridesharing.rideservice.event.RideEventPublisher;
import com.ridesharing.rideservice.model.Ride;
import com.ridesharing.rideservice.model.RideStatus;
import com.ridesharing.rideservice.repository.RideRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final RideRepository rideRepository;
    private final RestTemplate restTemplate;
    private final RideEventPublisher eventPublisher;

    // =================== BOOK A RIDE ===================
    @CircuitBreaker(name = "pricingService", fallbackMethod = "bookRideFallback")
    @Bulkhead(name = "pricingService")
    public RideResponse bookRide(RideBookingRequest request) {
        double distanceInKm = calculateDistance(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude()
        );
        double durationMinutes = estimateDuration(distanceInKm);

        // INTER-SERVICE CALL → Pricing Service (via Eureka)
        Map<String, Object> pricingRequest = Map.of(
                "distanceInKm", distanceInKm,
                "estimatedDurationMinutes", durationMinutes,
                "vehicleType", request.getVehicleType()
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> pricingResponse = restTemplate.postForObject(
                "http://pricing-service/pricing/calculate",
                pricingRequest,
                Map.class
        );

        Double fare = pricingResponse != null ? ((Number) pricingResponse.get("totalFare")).doubleValue() : 0.0;
        String fareBreakdown = pricingResponse != null ? (String) pricingResponse.get("fareBreakdown") : "N/A";

        Ride ride = Ride.builder()
                .riderId(request.getRiderId())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .dropoffLatitude(request.getDropoffLatitude())
                .dropoffLongitude(request.getDropoffLongitude())
                .pickupAddress(request.getPickupAddress())
                .dropoffAddress(request.getDropoffAddress())
                .vehicleType(request.getVehicleType())
                .distanceInKm(distanceInKm)
                .estimatedDurationMinutes(durationMinutes)
                .fare(fare)
                .fareBreakdown(fareBreakdown)
                .status(RideStatus.REQUESTED)
                .build();

        ride = rideRepository.save(ride);

        // Publish event to RabbitMQ
        eventPublisher.publishRideBooked(buildEvent(ride));

        log.info("Ride {} booked by rider {} — fare: ₹{}", ride.getId(), ride.getRiderId(), fare);
        return mapToResponse(ride);
    }

    // =================== CIRCUIT BREAKER FALLBACK ===================
    public RideResponse bookRideFallback(RideBookingRequest request, Throwable throwable) {
        log.warn("Circuit Breaker OPEN for pricing service. Booking with estimated fare. Error: {}", throwable.getMessage());

        double distanceInKm = calculateDistance(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude()
        );
        double estimatedFare = distanceInKm * 12.0;

        Ride ride = Ride.builder()
                .riderId(request.getRiderId())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .dropoffLatitude(request.getDropoffLatitude())
                .dropoffLongitude(request.getDropoffLongitude())
                .pickupAddress(request.getPickupAddress())
                .dropoffAddress(request.getDropoffAddress())
                .vehicleType(request.getVehicleType())
                .distanceInKm(distanceInKm)
                .estimatedDurationMinutes(estimateDuration(distanceInKm))
                .fare(estimatedFare)
                .fareBreakdown("Estimated fare (pricing service unavailable): ₹" + estimatedFare)
                .status(RideStatus.REQUESTED)
                .build();

        ride = rideRepository.save(ride);
        eventPublisher.publishRideBooked(buildEvent(ride));

        return mapToResponse(ride);
    }

    // =================== ACCEPT RIDE (Driver) ===================
    @CircuitBreaker(name = "driverLocationService", fallbackMethod = "acceptRideFallback")
    @Bulkhead(name = "driverLocationService")
    public RideResponse acceptRide(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new RuntimeException("Ride cannot be accepted. Current status: " + ride.getStatus());
        }

        // INTER-SERVICE CALL → Driver Location Service (via Eureka)
        restTemplate.postForObject(
                "http://driver-location-service/locations/update",
                Map.of("driverId", driverId, "latitude", ride.getPickupLatitude(),
                        "longitude", ride.getPickupLongitude(), "isAvailable", false),
                String.class
        );

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        log.info("Ride {} accepted by driver {}", rideId, driverId);
        return mapToResponse(ride);
    }

    public RideResponse acceptRideFallback(Long rideId, Long driverId, Throwable throwable) {
        log.warn("Circuit Breaker OPEN for driver location service. Accepting without location update. Error: {}", throwable.getMessage());

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        return mapToResponse(ride);
    }

    // =================== START RIDE ===================
    public RideResponse startRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new RuntimeException("Ride cannot be started. Current status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        log.info("Ride {} started", rideId);
        return mapToResponse(ride);
    }

    // =================== COMPLETE RIDE ===================
    public RideResponse completeRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new RuntimeException("Ride cannot be completed. Current status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride = rideRepository.save(ride);

        // Publish event → Payment & Notification services will consume
        eventPublisher.publishRideCompleted(buildEvent(ride));

        log.info("Ride {} completed. Fare: ₹{}", rideId, ride.getFare());
        return mapToResponse(ride);
    }

    // =================== CANCEL RIDE ===================
    public RideResponse cancelRide(Long rideId, String reason) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));

        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new RuntimeException("Ride cannot be cancelled. Current status: " + ride.getStatus());
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        ride.setCancellationReason(reason);
        ride = rideRepository.save(ride);

        eventPublisher.publishRideCancelled(buildEvent(ride));

        log.info("Ride {} cancelled. Reason: {}", rideId, reason);
        return mapToResponse(ride);
    }

    // =================== QUERY METHODS ===================
    public RideResponse getRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found: " + rideId));
        return mapToResponse(ride);
    }

    public List<RideResponse> getRiderHistory(Long riderId) {
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream().map(this::mapToResponse).toList();
    }

    public List<RideResponse> getDriverHistory(Long driverId) {
        return rideRepository.findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream().map(this::mapToResponse).toList();
    }

    // =================== HELPERS ===================
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return Math.round(distance * 100.0) / 100.0;
    }

    private double estimateDuration(double distanceInKm) {
        return Math.round(distanceInKm * 3.0 * 100.0) / 100.0;
    }

    private RideEvent buildEvent(Ride ride) {
        return RideEvent.builder()
                .rideId(ride.getId())
                .riderId(ride.getRiderId())
                .driverId(ride.getDriverId())
                .vehicleType(ride.getVehicleType())
                .fare(ride.getFare())
                .pickupAddress(ride.getPickupAddress())
                .dropoffAddress(ride.getDropoffAddress())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private RideResponse mapToResponse(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .riderId(ride.getRiderId())
                .driverId(ride.getDriverId())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .dropoffLatitude(ride.getDropoffLatitude())
                .dropoffLongitude(ride.getDropoffLongitude())
                .pickupAddress(ride.getPickupAddress())
                .dropoffAddress(ride.getDropoffAddress())
                .status(ride.getStatus())
                .vehicleType(ride.getVehicleType())
                .distanceInKm(ride.getDistanceInKm())
                .estimatedDurationMinutes(ride.getEstimatedDurationMinutes())
                .fare(ride.getFare())
                .fareBreakdown(ride.getFareBreakdown())
                .createdAt(ride.getCreatedAt())
                .acceptedAt(ride.getAcceptedAt())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .build();
    }
}
