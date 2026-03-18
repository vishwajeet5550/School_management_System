package com.JwtBased.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FeeDTO {
    private Long studentId;
    private String feeType;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String remarks;
}