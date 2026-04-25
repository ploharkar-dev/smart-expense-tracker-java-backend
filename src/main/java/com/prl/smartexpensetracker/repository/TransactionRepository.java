package com.prl.smartexpensetracker.repository;

import com.prl.smartexpensetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndTxnDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.userId = :userId AND YEAR(t.txnDate) = :year AND MONTH(t.txnDate) = :month")
    BigDecimal getTotalAmountByUserAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.userId = :userId")
    BigDecimal getTotalAmountByUser(@Param("userId") Long userId);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.user.userId = :userId")
    BigDecimal getAverageAmountByUser(@Param("userId") Long userId);
}
