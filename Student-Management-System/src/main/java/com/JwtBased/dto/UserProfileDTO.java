package com.JwtBased.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileDTO {

    // Common user fields
    private Long id;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String department;
    private String profilePicture;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Student-specific (null if not student)
    private String rollNumber;
    private String className;
    private String section;
    private String parentName;
    private String parentPhone;
    private LocalDate admissionDate;

    // Teacher-specific (null if not teacher)
    private String subject;
    private String qualification;
    private LocalDate joiningDate;
    private BigDecimal salary;
}