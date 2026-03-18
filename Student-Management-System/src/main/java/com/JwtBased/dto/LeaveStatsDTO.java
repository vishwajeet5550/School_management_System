// LeaveStatsDTO.java — Admin Dashboard
package com.JwtBased.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveStatsDTO {
    private long totalLeaves;
    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;
    private long autoRejectedLeaves;
    private long cancelledLeaves;
    private long studentLeaves;
    private long teacherLeaves;
    private long sickLeaves;
    private long personalLeaves;
    private long familyLeaves;
    private long otherLeaves;
}