package com.JwtBased.controller;

import com.JwtBased.dto.*;
import com.JwtBased.entity.Exam;
import com.JwtBased.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Exams & Marks", description = "Exam and result management APIs")
public class ExamController {

    private final ExamService examService;

    // ── Create Exam ────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Create new exam")
    public ResponseEntity<ApiResponse<ExamDTO>> createExam(
            @RequestBody ExamDTO dto) {
        // ✅ auth parameter nahi — SecurityUtils internally getCurrentUser() karto
        return ResponseEntity.ok(examService.createExam(dto));
    }

    // ── Get All Exams ──────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get all exams")
    public ResponseEntity<ApiResponse<List<Exam>>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    // ── Get Exams By Class ─────────────────────────────────
    @GetMapping("/class/{className}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Operation(summary = "Get exams by class")
    public ResponseEntity<ApiResponse<List<Exam>>> getByClass(
            @PathVariable String className) {
        return ResponseEntity.ok(examService.getExamsByClass(className));
    }

    // ── Add Marks ──────────────────────────────────────────
    @PostMapping("/marks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Add student marks")
    public ResponseEntity<ApiResponse<MarkReportDTO>> addMarks(
            @RequestBody MarkDTO dto) {
        return ResponseEntity.ok(examService.addMarks(dto));
    }

    // ── Update Marks ───────────────────────────────────────
    @PutMapping("/marks/{markId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Update student marks")
    public ResponseEntity<ApiResponse<MarkReportDTO>> updateMarks(
            @PathVariable Long markId,
            @RequestBody MarkDTO dto) {
        return ResponseEntity.ok(examService.updateMarks(markId, dto));
    }

    // ── Get Exam Results ───────────────────────────────────
    @GetMapping("/{examId}/results")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get all results for an exam")
    public ResponseEntity<ApiResponse<List<MarkReportDTO>>> getExamResults(
            @PathVariable Long examId) {
        return ResponseEntity.ok(examService.getExamResults(examId));
    }

    // ── Get Toppers ────────────────────────────────────────
    @GetMapping("/{examId}/toppers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get exam toppers")
    public ResponseEntity<ApiResponse<List<MarkReportDTO>>> getToppers(
            @PathVariable Long examId) {
        return ResponseEntity.ok(examService.getToppers(examId));
    }

    // ── Get Student Results (Admin/Teacher) ────────────────
    @GetMapping("/student/{studentId}/results")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Operation(summary = "Get all results for a student")
    public ResponseEntity<ApiResponse<List<MarkReportDTO>>> getStudentResults(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(examService.getStudentResults(studentId));
    }

    // ── Get Own Results (Student only) ────────────────────
    @GetMapping("/my-results")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student - Get own results")
    public ResponseEntity<ApiResponse<List<MarkReportDTO>>> getOwnResults() {
        // ✅ auth parameter nahi — SecurityUtils internally getCurrentUserId() karto
        return ResponseEntity.ok(examService.getOwnResults());
    }
}