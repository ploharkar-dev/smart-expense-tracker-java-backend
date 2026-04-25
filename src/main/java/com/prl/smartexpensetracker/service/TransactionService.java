package com.prl.smartexpensetracker.service;

import com.prl.smartexpensetracker.dto.TransactionDTO;
import com.prl.smartexpensetracker.entity.Category;
import com.prl.smartexpensetracker.entity.Transaction;
import com.prl.smartexpensetracker.entity.User;
import com.prl.smartexpensetracker.exception.InvalidInputException;
import com.prl.smartexpensetracker.exception.ResourceNotFoundException;
import com.prl.smartexpensetracker.repository.CategoryRepository;
import com.prl.smartexpensetracker.repository.TransactionRepository;
import com.prl.smartexpensetracker.repository.UserRepository;
import com.prl.smartexpensetracker.util.CategoryClassifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryClassifier categoryClassifier;

    /**
     * Add a new transaction.
     *
     * @param transactionDTO Transaction details
     * @return Created transaction DTO
     */
    public TransactionDTO addTransaction(TransactionDTO transactionDTO) {
        // Validate
        if (transactionDTO.getAmount() == null || transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Amount must be greater than 0");
        }
        if (transactionDTO.getTxnDate() == null) {
            throw new InvalidInputException("Transaction date is required");
        }

        // Find user
        User user = userRepository.findById(transactionDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Find or create category
        Category category = categoryRepository.findById(transactionDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Create transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(transactionDTO.getAmount())
                .description(transactionDTO.getDescription())
                .txnDate(transactionDTO.getTxnDate())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Get auto-suggested category
        String suggestedCategory = categoryClassifier.classifyCategory(transactionDTO.getDescription());

        return TransactionDTO.builder()
                .txnId(savedTransaction.getTxnId())
                .userId(savedTransaction.getUser().getUserId())
                .categoryId(savedTransaction.getCategory().getCategoryId())
                .categoryName(savedTransaction.getCategory().getCategoryName())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .txnDate(savedTransaction.getTxnDate())
                .suggestedCategory(suggestedCategory)
                .build();
    }

    /**
     * Fetch all transactions for a user.
     *
     * @param userId User ID
     * @return List of transactions
     */
    public List<TransactionDTO> fetchAllTransactions(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetch transactions within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @return List of transactions within the date range
     */
    public List<TransactionDTO> fetchTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return transactionRepository.findByUserIdAndTxnDateBetween(userId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update a transaction.
     *
     * @param txnId            Transaction ID
     * @param transactionDTO   Updated transaction details
     * @return Updated transaction DTO
     */
    public TransactionDTO updateTransaction(Long txnId, TransactionDTO transactionDTO) {
        Transaction transaction = transactionRepository.findById(txnId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Validate
        if (transactionDTO.getAmount() != null && transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Amount must be greater than 0");
        }

        if (transactionDTO.getAmount() != null) {
            transaction.setAmount(transactionDTO.getAmount());
        }
        if (transactionDTO.getDescription() != null) {
            transaction.setDescription(transactionDTO.getDescription());
        }
        if (transactionDTO.getTxnDate() != null) {
            transaction.setTxnDate(transactionDTO.getTxnDate());
        }
        if (transactionDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(transactionDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            transaction.setCategory(category);
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return convertToDTO(updatedTransaction);
    }

    /**
     * Delete a transaction.
     *
     * @param txnId Transaction ID
     */
    public void deleteTransaction(Long txnId) {
        Transaction transaction = transactionRepository.findById(txnId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    /**
     * Get transaction summary for a user.
     *
     * @param userId User ID
     * @return Summary with total, average, and count
     */
    public java.util.Map<String, Object> getTransactionSummary(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal totalAmount = transactionRepository.getTotalAmountByUser(userId);
        BigDecimal averageAmount = transactionRepository.getAverageAmountByUser(userId);
        int count = transactionRepository.findByUserId(userId).size();

        return java.util.Map.of(
                "totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO,
                "averageAmount", averageAmount != null ? averageAmount : BigDecimal.ZERO,
                "transactionCount", count
        );
    }

    /**
     * Get monthly spending summary.
     *
     * @param userId  User ID
     * @param year    Year
     * @param month   Month (1-12)
     * @return Monthly spending amount
     */
    public BigDecimal getMonthlySpending(Long userId, int year, int month) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal amount = transactionRepository.getTotalAmountByUserAndMonth(userId, year, month);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .txnId(transaction.getTxnId())
                .userId(transaction.getUser().getUserId())
                .categoryId(transaction.getCategory().getCategoryId())
                .categoryName(transaction.getCategory().getCategoryName())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .txnDate(transaction.getTxnDate())
                .build();
    }
}
