package com.JwtBased.repository;

import com.JwtBased.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkRepo extends JpaRepository<Mark, Long> {

    List<Mark> findByStudentId(Long studentId);
    List<Mark> findByExamId(Long examId);
    boolean existsByStudentIdAndExamId(Long studentId, Long examId);
    Optional<Mark> findByStudentIdAndExamId(Long studentId, Long examId);

    // Get all marks for a student in a class
    @Query("SELECT m FROM Mark m WHERE " +
            "m.student.className = :className AND " +
            "m.exam.examName = :examName")
    List<Mark> findByClassAndExam(
            @Param("className") String className,
            @Param("examName") String examName);

    // Toppers
    @Query("SELECT m FROM Mark m WHERE " +
            "m.exam.id = :examId " +
            "ORDER BY m.obtainedMarks DESC")
    List<Mark> findToppersByExam(@Param("examId") Long examId);
}