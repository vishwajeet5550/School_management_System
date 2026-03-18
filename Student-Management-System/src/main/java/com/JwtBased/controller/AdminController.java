package com.JwtBased.controller;

import com.JwtBased.dto.*;
import com.JwtBased.service.AdminService;
import com.JwtBased.service.StudentService;
import com.JwtBased.service.TeacherService;
import com.JwtBased.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// ✅ List import REMOVE karo — no longer needed
// import java.util.List; ← DELETE THIS

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin", description = "Admin management APIs")
public class AdminController {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final AdminService adminService;
    private final SecurityUtils securityUtils;

    // ── Dashboard ──────────────────────────────────────────
    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        String username = securityUtils.getCurrentUsername();
        return ResponseEntity.ok(
                ApiResponse.success("Welcome " + username, "Admin Dashboard"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get admin own profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getAdminProfile() {
        return ResponseEntity.ok(adminService.getAdminProfile());
    }

    // ════════════════════════════════════════
    //  STUDENT APIs
    // ════════════════════════════════════════

    @PostMapping(value = "/students", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new student",
            requestBody = @RequestBody(content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "profilePicture",
                            contentType = "image/jpeg, image/png, image/jpg"))))
    public ResponseEntity<ApiResponse<UserProfileDTO>> createStudent(
            @ModelAttribute StudentDTO dto) {
        return ResponseEntity.ok(studentService.createStudent(dto));
    }

    // ✅ Fix — ResponseEntity wildcard use karo
    @GetMapping("/students")
    @Operation(summary = "Get all students with pagination")
    public ResponseEntity<?> getAllStudents(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(
                studentService.getAllStudents(page, size, sortBy, sortDir));
    }

    // ✅ Fix — ResponseEntity wildcard use karo
    @GetMapping("/students/search")
    @Operation(summary = "Search students with pagination — single char supported")
    public ResponseEntity<?> searchStudents(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                studentService.searchStudents(keyword, page, size));
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getStudentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PutMapping(value = "/students/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update student",
            requestBody = @RequestBody(content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "profilePicture",
                            contentType = "image/jpeg, image/png, image/jpg"))))
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateStudent(
            @PathVariable Long id,
            @ModelAttribute StudentDTO dto) {
        return ResponseEntity.ok(studentService.updateStudent(id, dto));
    }

    @DeleteMapping("/students/{id}")
    @Operation(summary = "Deactivate student")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.deleteStudent(id));
    }

    // ════════════════════════════════════════
    //  TEACHER APIs
    // ════════════════════════════════════════

    @PostMapping(value = "/teachers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new teacher",
            requestBody = @RequestBody(content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "profilePicture",
                            contentType = "image/jpeg, image/png, image/jpg"))))
    public ResponseEntity<ApiResponse<UserProfileDTO>> createTeacher(
            @ModelAttribute TeacherDTO dto) {
        return ResponseEntity.ok(teacherService.createTeacher(dto));
    }

    // ✅ Fix — ResponseEntity wildcard use karo
    @GetMapping("/teachers")
    @Operation(summary = "Get all teachers with pagination")
    public ResponseEntity<?> getAllTeachers(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(
                teacherService.getAllTeachers(page, size, sortBy, sortDir));
    }

    // ✅ Fix — ResponseEntity wildcard use karo
    @GetMapping("/teachers/search")
    @Operation(summary = "Search teachers with pagination — single char supported")
    public ResponseEntity<?> searchTeachers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                teacherService.searchTeachers(keyword, page, size));
    }

    @GetMapping("/teachers/{id}")
    @Operation(summary = "Get teacher by ID")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getTeacherById(
            @PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @PutMapping(value = "/teachers/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update teacher",
            requestBody = @RequestBody(content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "profilePicture",
                            contentType = "image/jpeg, image/png, image/jpg"))))
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateTeacher(
            @PathVariable Long id,
            @ModelAttribute TeacherDTO dto) {
        return ResponseEntity.ok(teacherService.updateTeacher(id, dto));
    }

    @DeleteMapping("/teachers/{id}")
    @Operation(summary = "Deactivate teacher")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.deleteTeacher(id));
    }
}