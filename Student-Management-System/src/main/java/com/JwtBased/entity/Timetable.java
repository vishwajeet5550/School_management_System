package com.JwtBased.entity;

import com.JwtBased.enums.Day; // ✅ Import from enums package
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "timetable",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"class_name", "section", "day", "time_slot"}
        ))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(nullable = false)
    private String section;

    @Column(nullable = false)
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Day day; // ✅ enums.Day use karo

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    @Column(nullable = false)
    private String room;

    private Boolean isActive = true;

    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    // ❌ Inner enum REMOVE kela
    // public enum Day { ... } → DELETE
}