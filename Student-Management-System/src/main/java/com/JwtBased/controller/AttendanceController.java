package com.JwtBased.controller;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.AttendanceDTO;
import com.JwtBased.dto.AttendanceReportDTO;
import com.JwtBased.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Attendance", description = "Attendance management APIs")
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ── Mark Attendance ────────────────────────────────────
    @PostMapping("/mark")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Mark student attendance")
    public ResponseEntity<ApiResponse<AttendanceReportDTO>> markAttendance(
            @RequestBody AttendanceDTO dto) {
        // ✅ auth nahi — SecurityUtils internally getCurrentUser() karto
        return ResponseEntity.ok(attendanceService.markAttendance(dto));
    }

    // ── Update Attendance ──────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Update attendance record")
    public ResponseEntity<ApiResponse<AttendanceReportDTO>> updateAttendance(
            @PathVariable Long id,
            @RequestBody AttendanceDTO dto) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, dto));
    }

    // ── Get By Class And Date ──────────────────────────────
    @GetMapping("/class")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get attendance by class and date")
    public ResponseEntity<ApiResponse<List<AttendanceReportDTO>>> getByClassAndDate(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceByClassAndDate(className, section, date));
    }

    // ── Get Student Report ─────────────────────────────────
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get student attendance report")
    public ResponseEntity<ApiResponse<AttendanceReportDTO>> getStudentReport(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(
                attendanceService.getStudentAttendanceReport(studentId));
    }

    // ── Get By Date Range ──────────────────────────────────
    @GetMapping("/student/{studentId}/range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get attendance by date range")
    public ResponseEntity<ApiResponse<List<AttendanceReportDTO>>> getByDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
                attendanceService.getStudentAttendanceByDateRange(
                        studentId, startDate, endDate));
    }

    // ── Get Own Attendance (Student) ───────────────────────
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student - Get own attendance report")
    public ResponseEntity<ApiResponse<AttendanceReportDTO>> getOwnAttendance() {
        // ✅ auth nahi — SecurityUtils internally getCurrentUserId() karto
        return ResponseEntity.ok(attendanceService.getOwnAttendance());
    }
}