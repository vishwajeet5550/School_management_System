package com.JwtBased.controller;

import com.JwtBased.dto.AnnouncementDTO;
import com.JwtBased.dto.AnnouncementResponseDTO;
import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.PageResponseDTO;
import com.JwtBased.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Announcements", description = "School notice board APIs")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // ════════════════════════════════════════
    //  ADMIN APIs
    // ════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Create announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponseDTO>> create(
            @RequestBody AnnouncementDTO dto) {
        return ResponseEntity.ok(
                announcementService.createAnnouncement(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Update announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponseDTO>> update(
            @PathVariable Long id,
            @RequestBody AnnouncementDTO dto) {
        return ResponseEntity.ok(
                announcementService.updateAnnouncement(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Delete announcement")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                announcementService.deleteAnnouncement(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Get all announcements paginated")
    public ResponseEntity<?> getAllAnnouncements(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                announcementService.getAllAnnouncements(page, size));
    }

    // ════════════════════════════════════════
    //  STUDENT & TEACHER APIs
    // ════════════════════════════════════════

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get my relevant announcements")
    public ResponseEntity<ApiResponse<List<AnnouncementResponseDTO>>>
    getMyAnnouncements() {
        // ✅ SecurityUtils internally role detect karto
        return ResponseEntity.ok(
                announcementService.getMyAnnouncements());
    }

    @GetMapping("/my/paginated")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get my announcements paginated")
    public ResponseEntity<?> getMyAnnouncementsPaginated(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                announcementService.getMyAnnouncementsPageable(page, size));
    }

    @GetMapping("/my/high-priority")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get high priority notices")
    public ResponseEntity<ApiResponse<List<AnnouncementResponseDTO>>>
    getHighPriority() {
        return ResponseEntity.ok(
                announcementService.getHighPriorityNotices());
    }
}