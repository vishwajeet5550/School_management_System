package com.JwtBased.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceReportDTO {
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String className;
    private String section;
    private LocalDate date;
    private String status;
    private String remarks;
    private String markedBy;

    // Summary fields
    private Long totalDays;
    private Long presentDays;
    private Long absentDays;
    private Long lateDays;
    private Double attendancePercentage;
}