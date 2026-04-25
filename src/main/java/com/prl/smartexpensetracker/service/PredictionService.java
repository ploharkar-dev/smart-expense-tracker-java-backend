package com.prl.smartexpensetracker.service;

import com.prl.smartexpensetracker.dto.PredictionDTO;
import com.prl.smartexpensetracker.entity.Prediction;
import com.prl.smartexpensetracker.entity.User;
import com.prl.smartexpensetracker.exception.ResourceNotFoundException;
import com.prl.smartexpensetracker.repository.PredictionRepository;
import com.prl.smartexpensetracker.repository.TransactionRepository;
import com.prl.smartexpensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Generate savings forecast for a user.
     * Uses simple moving average of past 3 months spending.
     *
     * @param userId User ID
     * @return List of predictions for next 3 months
     */
    public List<PredictionDTO> generateForecast(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get last 3 months average spending
        YearMonth currentMonth = YearMonth.now();
        BigDecimal avgSpending = calculateThreeMonthAverage(userId, currentMonth);

        // Generate predictions for next 3 months
        List<Prediction> predictions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            YearMonth futureMonth = currentMonth.plusMonths(i);
            
            // Calculate confidence score (decreases over time)
            BigDecimal confidenceScore = new BigDecimal(100 - (i * 10));

            Prediction prediction = Prediction.builder()
                    .user(user)
                    .forecastedAmount(avgSpending)
                    .forecastedMonth(futureMonth)
                    .confidenceScore(confidenceScore)
                    .build();

            predictions.add(prediction);
        }

        // Save predictions
        List<Prediction> savedPredictions = predictionRepository.saveAll(predictions);

        return savedPredictions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get latest forecast for a user.
     *
     * @param userId User ID
     * @return Latest prediction
     */
    public PredictionDTO getLatestForecast(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return predictionRepository.findLatestByUserId(userId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No forecast found"));
    }

    /**
     * Get all forecasts for a user.
     *
     * @param userId User ID
     * @return List of predictions
     */
    public List<PredictionDTO> getAllForecasts(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return predictionRepository.findByUserIdOrderByForecastedMonthDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculate average spending for last 3 months.
     *
     * @param userId      User ID
     * @param currentMonth Current month
     * @return Average spending amount
     */
    private BigDecimal calculateThreeMonthAverage(Long userId, YearMonth currentMonth) {
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 1; i <= 3; i++) {
            YearMonth month = currentMonth.minusMonths(i);
            BigDecimal monthlySpending = transactionRepository.getTotalAmountByUserAndMonth(
                    userId,
                    month.getYear(),
                    month.getMonthValue()
            );
            if (monthlySpending != null) {
                total = total.add(monthlySpending);
            }
        }

        return total.divide(new BigDecimal(3), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculate linear regression forecast (advanced).
     *
     * @param userId User ID
     * @return Predicted spending for next month
     */
    public BigDecimal predictNextMonthSpending(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        YearMonth currentMonth = YearMonth.now();

        // Get last 6 months spending
        List<BigDecimal> monthlyData = new ArrayList<>();
        for (int i = 6; i >= 1; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            BigDecimal spending = transactionRepository.getTotalAmountByUserAndMonth(
                    userId,
                    month.getYear(),
                    month.getMonthValue()
            );
            monthlyData.add(spending != null ? spending : BigDecimal.ZERO);
        }

        // Simple average for now
        return monthlyData.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(monthlyData.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private PredictionDTO convertToDTO(Prediction prediction) {
        return PredictionDTO.builder()
                .predictionId(prediction.getPredictionId())
                .userId(prediction.getUser().getUserId())
                .forecastedAmount(prediction.getForecastedAmount())
                .forecastedMonth(prediction.getForecastedMonth())
                .confidenceScore(prediction.getConfidenceScore())
                .build();
    }
}
