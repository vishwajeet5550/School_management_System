package com.JwtBased.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marks",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"student_id", "exam_id"}
        ))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false)
    private Integer obtainedMarks;

    @Column(nullable = false)
    private String grade;         // A, B, C, D, F

    private String remarks;

    @Column(nullable = false)
    private Boolean isPassed;

    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}