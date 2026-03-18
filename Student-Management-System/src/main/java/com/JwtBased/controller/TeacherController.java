package com.JwtBased.controller;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.UserProfileDTO;
import com.JwtBased.service.TeacherService;
import com.JwtBased.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Teacher", description = "Teacher self-service APIs")
public class TeacherController {

    private final TeacherService teacherService;
    private final SecurityUtils securityUtils; // ✅ Add

    @GetMapping("/profile")
    @Operation(summary = "Get own profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile() {
        return ResponseEntity.ok(teacherService.getTeacherProfile());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Teacher dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        // ✅ auth.getName() → securityUtils.getCurrentUsername()
        String username = securityUtils.getCurrentUsername();
        return ResponseEntity.ok(
                ApiResponse.success("Welcome " + username, "Teacher Dashboard")
        );
    }
}