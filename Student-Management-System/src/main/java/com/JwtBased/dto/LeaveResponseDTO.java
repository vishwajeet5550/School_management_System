// LeaveResponseDTO.java — OUTPUT
package com.JwtBased.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveResponseDTO {
    private Long id;
    private Long userId;
    private String userName;        // Student/Teacher name
    private String userRole;        // STUDENT or TEACHER
    private String className;       // Student saathi only
    private String section;         // Student saathi only
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer totalDays;
    private String reason;
    private String status;
    private String adminRemark;
    private String reviewedByName;  // Who approved/rejected
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}