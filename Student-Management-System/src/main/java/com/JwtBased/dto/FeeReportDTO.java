package com.JwtBased.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FeeReportDTO {
    private Long feeId;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String className;
    private String feeType;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String remarks;

    // Summary
    private BigDecimal totalFees;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
}