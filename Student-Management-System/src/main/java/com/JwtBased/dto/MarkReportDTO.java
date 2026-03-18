package com.JwtBased.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MarkReportDTO {
    private Long markId;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String examName;
    private String subject;
    private String className;
    private LocalDate examDate;
    private Integer totalMarks;
    private Integer obtainedMarks;
    private Integer passingMarks;
    private String grade;
    private Boolean isPassed;
    private String remarks;
}