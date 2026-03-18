package com.JwtBased.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TimetableResponseDTO {

    private Long id;
    private String className;
    private String section;
    private String subject;
    private Long teacherId;
    private String teacherName;
    private String teacherPhone;
    private String day;
    private String timeSlot;
    private String room;
    private Boolean isActive;
}