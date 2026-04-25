package com.prl.smartexpensetracker.repository;

import com.prl.smartexpensetracker.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByUserId(Long userId);

    List<Prediction> findByUserIdOrderByForecastedMonthDesc(Long userId);

    Optional<Prediction> findByUserIdAndForecastedMonth(Long userId, YearMonth forecastedMonth);

    @Query("SELECT p FROM Prediction p WHERE p.user.userId = :userId ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Prediction> findLatestByUserId(@Param("userId") Long userId);
}
