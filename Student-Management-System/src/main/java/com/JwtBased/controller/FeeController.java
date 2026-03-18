package com.JwtBased.controller;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.FeeDTO;
import com.JwtBased.dto.FeeReportDTO;
import com.JwtBased.service.FeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Fee Management", description = "Fee management APIs")
public class FeeController {

    private final FeeService feeService;

    // ── Create Fee ─────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create fee record for student")
    public ResponseEntity<ApiResponse<FeeReportDTO>> createFee(
            @RequestBody FeeDTO dto) {
        return ResponseEntity.ok(feeService.createFee(dto));
    }

    // ── Pay Fee ────────────────────────────────────────────
    @PutMapping("/{feeId}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark fee as paid")
    public ResponseEntity<ApiResponse<FeeReportDTO>> payFee(
            @PathVariable Long feeId,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String transactionId) {
        return ResponseEntity.ok(
                feeService.payFee(feeId, paymentMethod, transactionId));
    }

    // ── Get All Fees ───────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all fee records")
    public ResponseEntity<ApiResponse<List<FeeReportDTO>>> getAllFees() {
        return ResponseEntity.ok(feeService.getAllFees());
    }

    // ── Get Pending Fees ───────────────────────────────────
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all pending fees")
    public ResponseEntity<ApiResponse<List<FeeReportDTO>>> getPendingFees() {
        return ResponseEntity.ok(feeService.getPendingFees());
    }

    // ── Get Overdue Fees ───────────────────────────────────
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all overdue fees")
    public ResponseEntity<ApiResponse<List<FeeReportDTO>>> getOverdueFees() {
        return ResponseEntity.ok(feeService.getOverdueFees());
    }

    // ── Get Student Fee Summary ────────────────────────────
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get fee summary for a student")
    public ResponseEntity<ApiResponse<FeeReportDTO>> getStudentFeeSummary(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(feeService.getStudentFeeSummary(studentId));
    }

    // ── Get Own Fees (Student) ─────────────────────────────
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student - Get own fee summary")
    public ResponseEntity<ApiResponse<FeeReportDTO>> getOwnFees() {
        // ✅ auth nahi — SecurityUtils internally getCurrentUserId() karto
        return ResponseEntity.ok(feeService.getOwnFees());
    }
}