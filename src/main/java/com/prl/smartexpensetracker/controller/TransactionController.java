package com.prl.smartexpensetracker.controller;

import com.prl.smartexpensetracker.dto.TransactionDTO;
import com.prl.smartexpensetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/add")
    public ResponseEntity<TransactionDTO> addTransaction(@RequestBody TransactionDTO transactionDTO) {
        TransactionDTO created = transactionService.addTransaction(transactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<TransactionDTO>> getTransactions(@PathVariable Long userId) {
        List<TransactionDTO> transactions = transactionService.fetchAllTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{userId}/range")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<TransactionDTO> transactions = transactionService.fetchTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{txnId}")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable Long txnId,
            @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO updated = transactionService.updateTransaction(txnId, transactionDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{txnId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long txnId) {
        transactionService.deleteTransaction(txnId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getTransactionSummary(@PathVariable Long userId) {
        Map<String, Object> summary = transactionService.getTransactionSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{userId}/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlySpending(
            @PathVariable Long userId,
            @PathVariable int year,
            @PathVariable int month) {
        java.math.BigDecimal spending = transactionService.getMonthlySpending(userId, year, month);
        return ResponseEntity.ok(Map.of("userId", userId, "year", year, "month", month, "spending", spending));
    }
}
