package com.JwtBased.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamDTO {
    private String examName;
    private String subject;
    private String className;
    private String section;
    private LocalDate examDate;
    private Integer totalMarks;
    private Integer passingMarks;
}