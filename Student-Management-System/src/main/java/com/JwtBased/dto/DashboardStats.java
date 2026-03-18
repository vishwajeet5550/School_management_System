package com.JwtBased.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStats {
    private long totalStudents;
    private long totalTeachers;
    private long totalAdmins;
    private long activeStudents;
    private long activeTeachers;
}