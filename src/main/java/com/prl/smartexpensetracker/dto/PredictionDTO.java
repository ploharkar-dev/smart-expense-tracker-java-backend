package com.prl.smartexpensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionDTO {
    private Long predictionId;
    private Long userId;
    private BigDecimal forecastedAmount;
    private YearMonth forecastedMonth;
    private BigDecimal confidenceScore;
}
