package com.ridesharing.ratingservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rideId", "ratedBy"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long rideId;

    @Column(nullable = false)
    private Long ratedBy;      // userId who gave the rating (rider or driver)

    @Column(nullable = false)
    private Long ratedUser;    // userId who received the rating

    @Column(nullable = false)
    private Integer score;     // 1 to 5 stars

    @Column(length = 500)
    private String review;     // optional text review

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
