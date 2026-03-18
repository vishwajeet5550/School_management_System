package com.JwtBased.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "exams")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String examName;      // e.g. "Mid Term", "Final Exam"

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String className;

    private String section;

    @Column(nullable = false)
    private LocalDate examDate;

    @Column(nullable = false)
    private Integer totalMarks;

    @Column(nullable = false)
    private Integer passingMarks;

    // Teacher who created exam
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
}