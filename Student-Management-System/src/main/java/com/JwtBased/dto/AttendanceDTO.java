package com.JwtBased.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceDTO {
    private Long studentId;
    private LocalDate date;
    private String status;   // PRESENT, ABSENT, LATE, HOLIDAY
    private String remarks;
}