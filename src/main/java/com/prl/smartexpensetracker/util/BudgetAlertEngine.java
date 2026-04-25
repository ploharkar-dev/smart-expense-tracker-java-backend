package com.prl.smartexpensetracker.util;

import com.prl.smartexpensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class BudgetAlertEngine {

    private final TransactionRepository transactionRepository;

    /**
     * Check if user has exceeded budget for the current month.
     *
     * @param userId          User ID
     * @param monthlyBudget   Monthly budget limit
     * @return true if spending exceeds budget, false otherwise
     */
    public boolean isBudgetExceeded(Long userId, BigDecimal monthlyBudget) {
        YearMonth currentMonth = YearMonth.now();
        BigDecimal currentMonthSpending = transactionRepository.getTotalAmountByUserAndMonth(
                userId,
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        );

        if (currentMonthSpending == null) {
            currentMonthSpending = BigDecimal.ZERO;
        }

        return currentMonthSpending.compareTo(monthlyBudget) > 0;
    }

    /**
     * Get the remaining budget for the current month.
     *
     * @param userId        User ID
     * @param monthlyBudget Monthly budget limit
     * @return Remaining budget (can be negative if exceeded)
     */
    public BigDecimal getRemainingBudget(Long userId, BigDecimal monthlyBudget) {
        YearMonth currentMonth = YearMonth.now();
        BigDecimal currentMonthSpending = transactionRepository.getTotalAmountByUserAndMonth(
                userId,
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        );

        if (currentMonthSpending == null) {
            currentMonthSpending = BigDecimal.ZERO;
        }

        return monthlyBudget.subtract(currentMonthSpending);
    }

    /**
     * Get spending percentage for the current month.
     *
     * @param userId        User ID
     * @param monthlyBudget Monthly budget limit
     * @return Spending percentage (0-100+)
     */
    public BigDecimal getSpendingPercentage(Long userId, BigDecimal monthlyBudget) {
        YearMonth currentMonth = YearMonth.now();
        BigDecimal currentMonthSpending = transactionRepository.getTotalAmountByUserAndMonth(
                userId,
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        );

        if (currentMonthSpending == null) {
            currentMonthSpending = BigDecimal.ZERO;
        }

        if (monthlyBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return currentMonthSpending.divide(monthlyBudget, 2, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
