package com.prl.smartexpensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long txnId;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private LocalDate txnDate;
    private String suggestedCategory;
}
