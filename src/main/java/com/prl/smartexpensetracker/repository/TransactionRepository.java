package com.prl.smartexpensetracker.repository;

import com.prl.smartexpensetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserUserId(Long userId);

    List<Transaction> findByUserUserIdAndTxnDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findByUserUserIdAndCategoryCategoryId(
            Long userId,
            Long categoryId
    );
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Transaction t WHERE t.user.userId = :userId")
    void deleteAllByUserId(Long userId);

    @Query("""
        SELECT SUM(t.amount)
        FROM Transaction t
        WHERE t.user.userId = :userId
        AND YEAR(t.txnDate) = :year
        AND MONTH(t.txnDate) = :month
    """)
    BigDecimal getTotalAmountByUserAndMonth(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("""
        SELECT SUM(t.amount)
        FROM Transaction t
        WHERE t.user.userId = :userId
    """)
    BigDecimal getTotalAmountByUser(@Param("userId") Long userId);

    @Query("""
        SELECT AVG(t.amount)
        FROM Transaction t
        WHERE t.user.userId = :userId
    """)
    BigDecimal getAverageAmountByUser(@Param("userId") Long userId);
}