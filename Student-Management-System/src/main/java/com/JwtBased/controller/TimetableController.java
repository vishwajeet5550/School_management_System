package com.JwtBased.controller;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.TimetableDTO;
import com.JwtBased.dto.TimetableResponseDTO;
import com.JwtBased.service.TimetableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Timetable", description = "Timetable and schedule management APIs")
public class TimetableController {

    private final TimetableService timetableService;

    // ════════════════════════════════════════
    //  ADMIN APIs
    // ════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Create timetable entry")
    public ResponseEntity<ApiResponse<TimetableResponseDTO>> createTimetable(
            @RequestBody TimetableDTO dto) {
        return ResponseEntity.ok(timetableService.createTimetable(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Update timetable entry")
    public ResponseEntity<ApiResponse<TimetableResponseDTO>> updateTimetable(
            @PathVariable Long id,
            @RequestBody TimetableDTO dto) {
        return ResponseEntity.ok(timetableService.updateTimetable(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Delete timetable entry")
    public ResponseEntity<ApiResponse<Void>> deleteTimetable(
            @PathVariable Long id) {
        return ResponseEntity.ok(timetableService.deleteTimetable(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Get all timetables")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getAllTimetables() {
        return ResponseEntity.ok(timetableService.getAllTimetables());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Get timetable by ID")
    public ResponseEntity<ApiResponse<TimetableResponseDTO>> getTimetableById(
            @PathVariable Long id) {
        return ResponseEntity.ok(timetableService.getTimetableById(id));
    }

    @GetMapping("/class")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Get timetable by class and section")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getByClass(
            @RequestParam String className,
            @RequestParam String section) {
        return ResponseEntity.ok(
                timetableService.getClassTimetable(className, section));
    }

    // ════════════════════════════════════════
    //  STUDENT APIs
    // ════════════════════════════════════════

    @GetMapping("/my-timetable")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student - Get own class timetable")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getOwnTimetable() {
        // ✅ SecurityUtils internally class & section fetch karto
        return ResponseEntity.ok(timetableService.getOwnClassTimetable());
    }


    // ── Student - Get Own Timetable By Day ────────────────
    @GetMapping("/my-timetable/day")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student - Get own timetable by day")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getOwnTimetableByDay(
            @RequestParam String day) {
        // ✅ day parameter pass karo — aadhi getOwnClassTimetable() chukiche hote
        return ResponseEntity.ok(timetableService.getOwnTimetableByDay(day));
    }

    // ════════════════════════════════════════
    //  TEACHER APIs
    // ════════════════════════════════════════

    @GetMapping("/my-schedule")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Teacher - Get own schedule")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getOwnSchedule() {
        // ✅ SecurityUtils internally teacher id fetch karto
        return ResponseEntity.ok(timetableService.getOwnSchedule());
    }

    @GetMapping("/my-schedule/day")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Teacher - Get own schedule by day")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getOwnScheduleByDay(
            @RequestParam String day) {
        return ResponseEntity.ok(timetableService.getOwnScheduleByDay(day));
    }
}