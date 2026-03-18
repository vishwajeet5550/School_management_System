package com.JwtBased.repository;

import com.JwtBased.entity.Leave;
import com.JwtBased.enums.LeaveStatus;
import com.JwtBased.enums.LeaveType;
import com.JwtBased.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepo extends JpaRepository<Leave, Long> {

    // ── Student/Teacher own leaves ─────────────────────────
    Page<Leave> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Leave> findByUserIdAndStatus(Long userId, LeaveStatus status);

    // ── Overlap check ──────────────────────────────────────
    @Query("SELECT l FROM Leave l WHERE " +
            "l.user.id = :userId AND " +
            "l.status NOT IN ('REJECTED', 'CANCELLED', 'AUTO_REJECTED') AND " +
            "(l.fromDate <= :toDate AND l.toDate >= :fromDate)")
    List<Leave> findOverlappingLeaves(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ── Admin — All leaves paginated ───────────────────────
    Page<Leave> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ── Admin — By status paginated ────────────────────────
    Page<Leave> findByStatusOrderByCreatedAtDesc(
            LeaveStatus status, Pageable pageable);

    // ── Admin — Student leaves only ────────────────────────
    @Query("SELECT l FROM Leave l WHERE " +
            "l.user.role = 'STUDENT' " +
            "ORDER BY l.createdAt DESC")
    Page<Leave> findStudentLeaves(Pageable pageable);

    // ── Teacher — Student pending leaves ──────────────────
    @Query("SELECT l FROM Leave l WHERE " +
            "l.user.role = 'STUDENT' AND " +
            "l.status = 'PENDING' " +
            "ORDER BY l.createdAt DESC")
    Page<Leave> findStudentPendingLeaves(Pageable pageable);

    // ── Admin — Teacher leaves only ────────────────────────
    @Query("SELECT l FROM Leave l WHERE " +
            "l.user.role = 'TEACHER' " +
            "ORDER BY l.createdAt DESC")
    Page<Leave> findTeacherLeaves(Pageable pageable);

    // ── Auto reject — fromDate = tomorrow, still PENDING ──
    @Query("SELECT l FROM Leave l WHERE " +
            "l.status = 'PENDING' AND " +
            "l.fromDate = :tomorrow")
    List<Leave> findPendingLeavesForTomorrow(
            @Param("tomorrow") LocalDate tomorrow);

    // ── Stats queries ──────────────────────────────────────
    long countByStatus(LeaveStatus status);
    long countByUserRole(Role role);
    long countByLeaveType(LeaveType leaveType);

    @Query("SELECT COUNT(l) FROM Leave l WHERE l.user.role = :role")
    long countByUserRole(@Param("role") String role);

    @Query("SELECT COUNT(l) FROM Leave l WHERE " +
            "l.user.id = :userId AND l.status = :status")
    long countByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") LeaveStatus status);
}