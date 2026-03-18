package com.JwtBased.repository;

import com.JwtBased.entity.Fee;
import com.JwtBased.enums.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeeRepo extends JpaRepository<Fee, Long> {

    List<Fee> findByStudentId(Long studentId);
    List<Fee> findByStudentIdAndStatus(Long studentId, FeeStatus status);
    List<Fee> findByStatus(FeeStatus status);

    // Overdue fees — due date passed and still pending
    @Query("SELECT f FROM Fee f WHERE " +
            "f.status = 'PENDING' AND f.dueDate < :today")
    List<Fee> findOverdueFees(@Param("today") LocalDate today);

    // Total collected fees
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fee f WHERE f.status = 'PAID'")
    BigDecimal getTotalCollectedFees();

    // Total pending fees
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fee f WHERE f.status = 'PENDING'")
    BigDecimal getTotalPendingFees();

    // Student fee summary
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fee f WHERE " +
            "f.student.id = :studentId AND f.status = 'PAID'")
    BigDecimal getPaidAmountByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fee f WHERE " +
            "f.student.id = :studentId AND f.status = 'PENDING'")
    BigDecimal getPendingAmountByStudent(@Param("studentId") Long studentId);
}