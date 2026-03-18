package com.JwtBased.controller;

import com.JwtBased.dto.*;
import com.JwtBased.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Leave Management", description = "Leave apply, approve, reject APIs")
public class LeaveController {

    private final LeaveService leaveService;

    // ════════════════════════════════════════
    //  STUDENT & TEACHER APIs
    // ════════════════════════════════════════

    @PostMapping("/apply")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    @Operation(summary = "Apply for leave")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> applyLeave(
            @RequestBody LeaveRequestDTO dto) {
        return ResponseEntity.ok(leaveService.applyLeave(dto));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    @Operation(summary = "Get own leave history")
    public ResponseEntity<?> getMyLeaves(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(leaveService.getMyLeaves(page, size));
    }

    @DeleteMapping("/my/{leaveId}/cancel")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    @Operation(summary = "Cancel own PENDING leave")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> cancelLeave(
            @PathVariable Long leaveId) {
        return ResponseEntity.ok(leaveService.cancelLeave(leaveId));
    }

    // ════════════════════════════════════════
    //  TEACHER APIs — Student leaves only
    // ════════════════════════════════════════

    @GetMapping("/teacher/students")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Teacher - View all student leaves")
    public ResponseEntity<?> getStudentLeavesForTeacher(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(leaveService.getStudentLeaves(page, size));
    }

    @GetMapping("/teacher/students/pending")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Teacher - View pending student leaves")
    public ResponseEntity<?> getStudentPendingLeaves(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                leaveService.getStudentPendingLeaves(page, size));
    }

    @PutMapping("/teacher/{leaveId}/approve")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Teacher - Approve student leave")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> teacherApproveLeave(
            @PathVariable Long leaveId,
            @RequestBody LeaveReviewDTO dto) {
        return ResponseEntity.ok(leaveService.approveLeave(leaveId, dto));
    }

    @PutMapping("/teacher/{leaveId}/reject")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Teacher - Reject student leave")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> teacherRejectLeave(
            @PathVariable Long leaveId,
            @RequestBody LeaveReviewDTO dto) {
        return ResponseEntity.ok(leaveService.rejectLeave(leaveId, dto));
    }

    // ════════════════════════════════════════
    //  ADMIN APIs — All leaves
    // ════════════════════════════════════════

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Get all leaves paginated")
    public ResponseEntity<?> getAllLeaves(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(required = false)     String status) {
        return ResponseEntity.ok(
                leaveService.getAllLeaves(page, size, status));
    }

    @GetMapping("/admin/students")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Get student leaves")
    public ResponseEntity<?> getStudentLeaves(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(leaveService.getStudentLeaves(page, size));
    }

    @GetMapping("/admin/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Get teacher leaves")
    public ResponseEntity<?> getTeacherLeaves(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(leaveService.getTeacherLeaves(page, size));
    }

    @PutMapping("/admin/{leaveId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Approve any leave")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> adminApproveLeave(
            @PathVariable Long leaveId,
            @RequestBody LeaveReviewDTO dto) {
        return ResponseEntity.ok(leaveService.approveLeave(leaveId, dto));
    }

    @PutMapping("/admin/{leaveId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Reject any leave")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> adminRejectLeave(
            @PathVariable Long leaveId,
            @RequestBody LeaveReviewDTO dto) {
        return ResponseEntity.ok(leaveService.rejectLeave(leaveId, dto));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Leave statistics")
    public ResponseEntity<ApiResponse<LeaveStatsDTO>> getLeaveStats() {
        return ResponseEntity.ok(leaveService.getLeaveStats());
    }
}