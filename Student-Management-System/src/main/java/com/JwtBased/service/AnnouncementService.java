package com.JwtBased.service;

import com.JwtBased.dto.AnnouncementDTO;
import com.JwtBased.dto.AnnouncementResponseDTO;
import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.PageResponseDTO;
import com.JwtBased.entity.Announcement;
import com.JwtBased.entity.User;
import com.JwtBased.repository.AnnouncementRepo;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepo announcementRepo;
    private final SecurityUtils securityUtils;

    // ── Create Announcement (ADMIN) ────────────────────────
    @Transactional
    public ApiResponse<AnnouncementResponseDTO> createAnnouncement(
            AnnouncementDTO dto) {

        String roleTarget = dto.getRoleTarget().toUpperCase();
        if (!roleTarget.equals("ALL") &&
                !roleTarget.equals("STUDENT") &&
                !roleTarget.equals("TEACHER")) {
            return ApiResponse.error(
                    "Invalid roleTarget! Use: ALL, STUDENT, TEACHER");
        }

        String priority = dto.getPriority() != null ?
                dto.getPriority().toUpperCase() : "MEDIUM";
        if (!priority.equals("HIGH") &&
                !priority.equals("MEDIUM") &&
                !priority.equals("LOW")) {
            priority = "MEDIUM";
        }

        User admin = securityUtils.getCurrentUser();

        Announcement announcement = Announcement.builder()
                .title(dto.getTitle())
                .message(dto.getMessage())
                .roleTarget(roleTarget)
                .createdBy(admin)
                .priority(priority)
                .isActive(true)
                .build();

        announcementRepo.save(announcement);
        return ApiResponse.success("Announcement created!", mapToDTO(announcement));
    }

    // ── Update Announcement (ADMIN) ────────────────────────
    @Transactional
    public ApiResponse<AnnouncementResponseDTO> updateAnnouncement(
            Long id, AnnouncementDTO dto) {

        Announcement announcement = announcementRepo.findById(id).orElse(null);
        if (announcement == null)
            return ApiResponse.error("Announcement not found");

        if (dto.getTitle() != null)
            announcement.setTitle(dto.getTitle());
        if (dto.getMessage() != null)
            announcement.setMessage(dto.getMessage());
        if (dto.getRoleTarget() != null)
            announcement.setRoleTarget(dto.getRoleTarget().toUpperCase());
        if (dto.getPriority() != null)
            announcement.setPriority(dto.getPriority().toUpperCase());

        announcementRepo.save(announcement);
        return ApiResponse.success("Announcement updated", mapToDTO(announcement));
    }

    // ── Delete Announcement (ADMIN) — Soft Delete ──────────
    @Transactional
    public ApiResponse<Void> deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepo.findById(id).orElse(null);
        if (announcement == null)
            return ApiResponse.error("Announcement not found");

        announcement.setIsActive(false);
        announcementRepo.save(announcement);
        return ApiResponse.success("Announcement deleted", null);
    }

    // ── Get All (ADMIN) — Paginated ────────────────────────
    public ApiResponse<PageResponseDTO<AnnouncementResponseDTO>> getAllAnnouncements(
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Announcement> announcementPage =
                announcementRepo.findByIsActiveTrue(pageable);

        List<AnnouncementResponseDTO> content = announcementPage.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        PageResponseDTO<AnnouncementResponseDTO> response =
                PageResponseDTO.<AnnouncementResponseDTO>builder()
                        .content(content)
                        .pageNumber(announcementPage.getNumber())
                        .pageSize(announcementPage.getSize())
                        .totalElements(announcementPage.getTotalElements())
                        .totalPages(announcementPage.getTotalPages())
                        .isFirst(announcementPage.isFirst())
                        .isLast(announcementPage.isLast())
                        .hasNext(announcementPage.hasNext())
                        .hasPrevious(announcementPage.hasPrevious())
                        .build();

        return ApiResponse.success("All announcements fetched", response);
    }

    // ── Get My Announcements ───────────────────────────────
    // ✅ OLD duplicate method DELETE kela — sirf he ek ahe
    public ApiResponse<List<AnnouncementResponseDTO>> getMyAnnouncements() {

        String role = securityUtils.getCurrentRole().name();
        List<AnnouncementResponseDTO> list;

        // ✅ ADMIN → sagle active records
        if (role.equals("ADMIN")) {
            list = announcementRepo.findByIsActiveTrueOrderByCreatedAtDesc()
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } else {
            // ✅ STUDENT/TEACHER → fakt relevant records
            list = announcementRepo.findRelevantAnnouncements(role)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        return ApiResponse.success(
                list.isEmpty() ? "No announcements" :
                        list.size() + " announcement(s) found", list);
    }

    // ── Get My Announcements Paginated ─────────────────────
    public ApiResponse<PageResponseDTO<AnnouncementResponseDTO>>
    getMyAnnouncementsPageable(int page, int size) {

        String role = securityUtils.getCurrentRole().name();

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Announcement> announcementPage;

        if (role.equals("ADMIN")) {
            announcementPage = announcementRepo.findByIsActiveTrue(pageable);
        } else {
            announcementPage = announcementRepo
                    .findRelevantAnnouncementsPageable(role, pageable);
        }

        List<AnnouncementResponseDTO> content = announcementPage.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        PageResponseDTO<AnnouncementResponseDTO> response =
                PageResponseDTO.<AnnouncementResponseDTO>builder()
                        .content(content)
                        .pageNumber(announcementPage.getNumber())
                        .pageSize(announcementPage.getSize())
                        .totalElements(announcementPage.getTotalElements())
                        .totalPages(announcementPage.getTotalPages())
                        .isFirst(announcementPage.isFirst())
                        .isLast(announcementPage.isLast())
                        .hasNext(announcementPage.hasNext())
                        .hasPrevious(announcementPage.hasPrevious())
                        .build();

        return ApiResponse.success("Announcements fetched", response);
    }

    // ── Get High Priority Notices ──────────────────────────
    // ✅ OLD duplicate method DELETE kela — sirf he ek ahe
    public ApiResponse<List<AnnouncementResponseDTO>> getHighPriorityNotices() {

        String role = securityUtils.getCurrentRole().name();
        List<AnnouncementResponseDTO> list;

        // ✅ ADMIN → sagle HIGH priority records
        if (role.equals("ADMIN")) {
            list = announcementRepo.findAllByPriority("HIGH")
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } else {
            // ✅ STUDENT/TEACHER → fakt relevant HIGH records
            list = announcementRepo.findByPriorityAndRole("HIGH", role)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        return ApiResponse.success(
                list.isEmpty() ? "No high priority notices" :
                        list.size() + " high priority notice(s) found", list);
    }

    // ── Mapper ─────────────────────────────────────────────
    private AnnouncementResponseDTO mapToDTO(Announcement a) {
        return AnnouncementResponseDTO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .message(a.getMessage())
                .roleTarget(a.getRoleTarget())
                .createdByName(a.getCreatedBy().getFullName())
                .priority(a.getPriority())
                .isActive(a.getIsActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
