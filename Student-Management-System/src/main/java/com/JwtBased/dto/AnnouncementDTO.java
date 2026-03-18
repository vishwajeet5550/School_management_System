package com.JwtBased.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AnnouncementDTO {

    private String title;
    private String message;
    private String roleTarget;  // ALL, STUDENT, TEACHER
    private String priority;    // HIGH, MEDIUM, LOW
}