package com.JwtBased.repository;

import com.JwtBased.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepo extends JpaRepository<Student, Long> {

    boolean existsByRollNumber(String rollNumber);
    Optional<Student> findByUserId(Long userId);

    // ✅ Pagination support add kela
    Page<Student> findAll(Pageable pageable);

    // ✅ Search with pagination
    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(s.user.fullName)    LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.user.username)    LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.user.email)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.user.phone)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.user.address)     LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.rollNumber)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.className)        LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.section)          LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.parentName)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.parentPhone)      LIKE LOWER(CONCAT('%',:k,'%'))")
    Page<Student> searchStudents(@Param("k") String keyword, Pageable pageable);

    // ✅ Without pagination (existing — search bar saathi)
    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(s.user.fullName)    LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.user.username)    LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.user.email)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.user.phone)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.rollNumber)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.className)        LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.section)          LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.parentName)       LIKE LOWER(CONCAT('%',:k,'%')) OR " +
            "LOWER(s.parentPhone)      LIKE LOWER(CONCAT('%',:k,'%'))")
    List<Student> searchStudents(@Param("k") String keyword);
}