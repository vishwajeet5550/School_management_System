package com.JwtBased.controller;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.UserProfileDTO;
import com.JwtBased.service.StudentService;
import com.JwtBased.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Student", description = "Student self-service APIs")
public class StudentController {

    private final StudentService studentService;
    private final SecurityUtils securityUtils; // ✅ Add

    @GetMapping("/profile")
    @Operation(summary = "Get own profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile() {
        return ResponseEntity.ok(studentService.getStudentProfile());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Student dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        // ✅ auth.getName() → securityUtils.getCurrentUsername()
        String username = securityUtils.getCurrentUsername();
        return ResponseEntity.ok(
                ApiResponse.success("Welcome " + username, "Student Dashboard")
        );
    }
}