package com.JwtBased.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentDTO {

    // User fields
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;

    // Student-specific fields
    private String rollNumber;
    private String className;
    private String section;
    private String parentName;
    private String parentPhone;
    private LocalDate admissionDate;

    // ✅ Profile picture — optional during creation

    @Schema(type = "string", format = "binary", description = "Profile picture file")
    private MultipartFile profilePicture;
}