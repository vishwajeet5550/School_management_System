package com.JwtBased.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AnnouncementResponseDTO {

    private Long id;
    private String title;
    private String message;
    private String roleTarget;
    private String createdByName;   // Admin ka naam
    private String priority;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
