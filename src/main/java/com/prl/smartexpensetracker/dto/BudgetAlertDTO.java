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
public class BudgetAlertDTO {
    private Long userId;
    private boolean budgetExceeded;
    private BigDecimal monthlyBudget;
    private BigDecimal currentSpending;
    private BigDecimal remainingBudget;
    private BigDecimal spendingPercentage;
    private String message;
}
