package com.JwtBased.repository;

import com.JwtBased.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepo extends JpaRepository<Exam, Long> {
    List<Exam> findByClassName(String className);
    List<Exam> findByClassNameAndSection(String className, String section);
    List<Exam> findBySubject(String subject);
    List<Exam> findByExamName(String examName);
}