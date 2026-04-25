package com.prl.smartexpensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryDTO {
    private Long userId;
    private String username;
    private BigDecimal totalSpending;
    private BigDecimal averageTransaction;
    private int transactionCount;
    private BigDecimal monthlyBudget;
    private BigDecimal remainingBudget;
    private BigDecimal spendingPercentage;
}
