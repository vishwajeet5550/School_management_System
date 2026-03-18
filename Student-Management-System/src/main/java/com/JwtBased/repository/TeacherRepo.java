package com.JwtBased.repository;

import com.JwtBased.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepo extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserId(Long userId);

    // ✅ Pagination support add kela
    Page<Teacher> findAll(Pageable pageable);

    // ✅ Search with pagination
    @Query("SELECT t FROM Teacher t WHERE " +
            "LOWER(t.user.fullName)      LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.user.username)      LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.user.email)         LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.user.phone)         LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.user.address)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.user.department)    LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.subject)            LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.qualification)      LIKE LOWER(CONCAT('%',:k,'%'))")
    Page<Teacher> searchTeachers(@Param("k") String keyword, Pageable pageable);

    // ✅ Without pagination (existing)
    @Query("SELECT t FROM Teacher t WHERE " +
            "LOWER(t.user.fullName)      LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.user.username)      LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.user.email)         LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.user.department)    LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.subject)            LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(t.qualification)      LIKE LOWER(CONCAT('%',:k,'%'))")
    List<Teacher> searchTeachers(@Param("k") String keyword);
}