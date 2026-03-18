package com.JwtBased.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "teachers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Join with users table via user_id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ✅ Only teacher-specific important fields
    // fullName, phone, address, DOB, profilePicture → already in users table
    private String subject;
    private String qualification;
    private LocalDate joiningDate;
    private BigDecimal salary;
}