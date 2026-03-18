package com.JwtBased.repository;

import com.JwtBased.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepo extends JpaRepository<Announcement, Long> {

    // ✅ Student/Teacher ke liye — role_target = "ALL" ya apna role
    @Query("SELECT a FROM Announcement a WHERE " +
            "a.isActive = true AND " +
            "(a.roleTarget = 'ALL' OR a.roleTarget = :role) " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findRelevantAnnouncements(@Param("role") String role);

    // ✅ Pagination with role filter
    @Query("SELECT a FROM Announcement a WHERE " +
            "a.isActive = true AND " +
            "(a.roleTarget = 'ALL' OR a.roleTarget = :role) " +
            "ORDER BY a.createdAt DESC")
    Page<Announcement> findRelevantAnnouncementsPageable(
            @Param("role") String role, Pageable pageable);

    // ✅ Admin — sagle active announcements
    List<Announcement> findByIsActiveTrueOrderByCreatedAtDesc();

    // ✅ Admin — paginated
    Page<Announcement> findByIsActiveTrue(Pageable pageable);

    // ✅ Student/Teacher — priority + role filter
    @Query("SELECT a FROM Announcement a WHERE " +
            "a.isActive = true AND " +
            "a.priority = :priority AND " +
            "(a.roleTarget = 'ALL' OR a.roleTarget = :role) " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findByPriorityAndRole(
            @Param("priority") String priority,
            @Param("role") String role);

    // ✅ NEW — Admin saathi — ALL records by priority
    @Query("SELECT a FROM Announcement a WHERE " +
            "a.isActive = true AND " +
            "a.priority = :priority " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findAllByPriority(@Param("priority") String priority);
}