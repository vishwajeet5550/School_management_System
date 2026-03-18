// LeaveReviewDTO.java — Approve/Reject INPUT
package com.JwtBased.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveReviewDTO {
    private String adminRemark;  // Required for reject, optional for approve
}