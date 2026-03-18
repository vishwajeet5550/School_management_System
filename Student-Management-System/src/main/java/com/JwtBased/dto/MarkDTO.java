package com.JwtBased.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MarkDTO {
    private Long studentId;
    private Long examId;
    private Integer obtainedMarks;
    private String remarks;
}