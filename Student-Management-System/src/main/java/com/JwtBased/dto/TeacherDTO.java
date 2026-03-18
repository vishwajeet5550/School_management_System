package com.JwtBased.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TeacherDTO {

    // User fields
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private String department;

    // Teacher-specific fields
    private String subject;
    private String qualification;
    private LocalDate joiningDate;
    private BigDecimal salary;

    // ✅ Profile picture — optional during creation
    @Schema(type = "string", format = "binary", description = "Profile picture file")
    private MultipartFile profilePicture;
}