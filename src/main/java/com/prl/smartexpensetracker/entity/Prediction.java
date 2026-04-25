package com.prl.smartexpensetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long predictionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal forecastedAmount;

    @Column(nullable = false)
    private YearMonth forecastedMonth;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "predictionId=" + predictionId +
                ", forecastedAmount=" + forecastedAmount +
                ", forecastedMonth=" + forecastedMonth +
                ", confidenceScore=" + confidenceScore +
                ", createdAt=" + createdAt +
                '}';
    }
}
