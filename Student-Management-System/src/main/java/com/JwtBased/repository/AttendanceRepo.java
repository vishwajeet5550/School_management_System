package com.JwtBased.repository;

import com.JwtBased.entity.Attendance;
import com.JwtBased.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, Long> {

    // Check if already marked
    boolean existsByStudentIdAndDate(Long studentId, LocalDate date);

    // Get attendance by student
    List<Attendance> findByStudentId(Long studentId);

    // Get attendance by student and date range
    List<Attendance> findByStudentIdAndDateBetween(
            Long studentId, LocalDate startDate, LocalDate endDate);

    // Get attendance by date (whole class)
    List<Attendance> findByDate(LocalDate date);

    // Get by class and date
    @Query("SELECT a FROM Attendance a WHERE " +
            "a.student.className = :className AND " +
            "a.student.section = :section AND " +
            "a.date = :date")
    List<Attendance> findByClassAndDate(
            @Param("className") String className,
            @Param("section") String section,
            @Param("date") LocalDate date);

    // Count by status for a student
    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    // Total attendance days for student
    long countByStudentId(Long studentId);

    Optional<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);
}