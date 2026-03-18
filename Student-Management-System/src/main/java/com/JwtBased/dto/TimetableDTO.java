package com.JwtBased.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TimetableDTO {

    // For create/update
    private String className;
    private String section;
    private String subject;
    private Long teacherId;   // Teacher ka User ID
    private String day;       // MONDAY, TUESDAY...
    private String timeSlot;  // "09:00-10:00"
    private String room;
}