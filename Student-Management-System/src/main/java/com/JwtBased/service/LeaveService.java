package com.JwtBased.service;

import com.JwtBased.dto.*;
import com.JwtBased.entity.Leave;
import com.JwtBased.entity.Student;
import com.JwtBased.entity.User;
import com.JwtBased.enums.LeaveStatus;
import com.JwtBased.enums.LeaveType;
import com.JwtBased.enums.Role;
import com.JwtBased.repository.LeaveRepo;
import com.JwtBased.repository.StudentRepo;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveRepo leaveRepo;
    private final StudentRepo studentRepo;
    private final SecurityUtils securityUtils;

    // ── Apply Leave (STUDENT/TEACHER) ──────────────────────
    @Transactional
    public ApiResponse<LeaveResponseDTO> applyLeave(LeaveRequestDTO dto) {

        User currentUser = securityUtils.getCurrentUser();

        // ✅ Validate leave type
        LeaveType leaveType;
        try {
            leaveType = LeaveType.valueOf(dto.getLeaveType().toUpperCase());
        } catch (Exception e) {
            return ApiResponse.error(
                    "Invalid leave type! Use: SICK, PERSONAL, FAMILY, OTHER");
        }

        // ✅ Date validations
        if (dto.getFromDate() == null || dto.getToDate() == null)
            return ApiResponse.error("From date and To date are required");

        if (dto.getFromDate().isBefore(LocalDate.now()))
            return ApiResponse.error("Cannot apply leave for past dates!");

        if (dto.getToDate().isBefore(dto.getFromDate()))
            return ApiResponse.error("To date cannot be before From date!");

        // ✅ Overlap check
        List<Leave> overlapping = leaveRepo.findOverlappingLeaves(
                currentUser.getId(), dto.getFromDate(), dto.getToDate());
        if (!overlapping.isEmpty())
            return ApiResponse.error(
                    "Leave already applied for these dates! Please check your existing leaves.");

        // ✅ Calculate total days
        int totalDays = (int) ChronoUnit.DAYS.between(
                dto.getFromDate(), dto.getToDate()) + 1;

        Leave leave = Leave.builder()
                .user(currentUser)
                .leaveType(leaveType)
                .fromDate(dto.getFromDate())
                .toDate(dto.getToDate())
                .totalDays(totalDays)
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        leaveRepo.save(leave);
        return ApiResponse.success(
                "Leave applied successfully! Total days: " + totalDays,
                mapToDTO(leave));
    }

    // ── Get Own Leaves (STUDENT/TEACHER) ──────────────────
    public ApiResponse<PageResponseDTO<LeaveResponseDTO>> getMyLeaves(
            int page, int size) {

        Long userId = securityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Leave> leavePage = leaveRepo
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return buildPageResponse(leavePage,
                "My leaves fetched — Page " + (page + 1));
    }

    // ── Cancel Leave (STUDENT/TEACHER) ────────────────────
    @Transactional
    public ApiResponse<LeaveResponseDTO> cancelLeave(Long leaveId) {
        Long userId = securityUtils.getCurrentUserId();

        Leave leave = leaveRepo.findById(leaveId).orElse(null);
        if (leave == null)
            return ApiResponse.error("Leave not found");

        // ✅ Own leave check
        if (!leave.getUser().getId().equals(userId))
            return ApiResponse.error("You can only cancel your own leave");

        // ✅ Fakt PENDING aselych cancel karta yenar
        if (leave.getStatus() != LeaveStatus.PENDING)
            return ApiResponse.error(
                    "Only PENDING leaves can be cancelled! " +
                            "Current status: " + leave.getStatus());

        leave.setStatus(LeaveStatus.CANCELLED);
        leaveRepo.save(leave);
        return ApiResponse.success("Leave cancelled successfully", mapToDTO(leave));
    }

    // ── Approve Leave (ADMIN + TEACHER for students) ───────
    @Transactional
    public ApiResponse<LeaveResponseDTO> approveLeave(
            Long leaveId, LeaveReviewDTO dto) {

        User reviewer = securityUtils.getCurrentUser();
        Leave leave = leaveRepo.findById(leaveId).orElse(null);

        if (leave == null)
            return ApiResponse.error("Leave not found");

        if (leave.getStatus() != LeaveStatus.PENDING)
            return ApiResponse.error(
                    "Only PENDING leaves can be approved! " +
                            "Current status: " + leave.getStatus());

        // ✅ Teacher fakt Student leaves approve karu shakto
        if (reviewer.getRole() == Role.TEACHER &&
                leave.getUser().getRole() != Role.STUDENT) {
            return ApiResponse.error(
                    "Teachers can only approve Student leaves!");
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setAdminRemark(dto.getAdminRemark() != null ?
                dto.getAdminRemark() : "Approved");
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(LocalDateTime.now());

        leaveRepo.save(leave);
        return ApiResponse.success("Leave approved successfully!", mapToDTO(leave));
    }

    // ── Reject Leave (ADMIN + TEACHER for students) ────────
    @Transactional
    public ApiResponse<LeaveResponseDTO> rejectLeave(
            Long leaveId, LeaveReviewDTO dto) {

        User reviewer = securityUtils.getCurrentUser();
        Leave leave = leaveRepo.findById(leaveId).orElse(null);

        if (leave == null)
            return ApiResponse.error("Leave not found");

        if (leave.getStatus() != LeaveStatus.PENDING)
            return ApiResponse.error(
                    "Only PENDING leaves can be rejected! " +
                            "Current status: " + leave.getStatus());

        // ✅ Teacher fakt Student leaves reject karu shakto
        if (reviewer.getRole() == Role.TEACHER &&
                leave.getUser().getRole() != Role.STUDENT) {
            return ApiResponse.error(
                    "Teachers can only reject Student leaves!");
        }

        // ✅ Reject saathi remark compulsory
        if (dto.getAdminRemark() == null || dto.getAdminRemark().trim().isEmpty())
            return ApiResponse.error("Remark is required when rejecting leave!");

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setAdminRemark(dto.getAdminRemark());
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(LocalDateTime.now());

        leaveRepo.save(leave);
        return ApiResponse.success("Leave rejected", mapToDTO(leave));
    }

    // ── Get All Leaves (ADMIN only) — Paginated ────────────
    public ApiResponse<PageResponseDTO<LeaveResponseDTO>> getAllLeaves(
            int page, int size, String status) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Leave> leavePage;

        if (status != null && !status.isEmpty()) {
            try {
                LeaveStatus leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
                leavePage = leaveRepo.findByStatusOrderByCreatedAtDesc(
                        leaveStatus, pageable);
            } catch (Exception e) {
                return ApiResponse.error("Invalid status!");
            }
        } else {
            leavePage = leaveRepo.findAllByOrderByCreatedAtDesc(pageable);
        }

        return buildPageResponse(leavePage, "All leaves fetched");
    }

    // ── Get Student Leaves (ADMIN + TEACHER) — Paginated ──
    public ApiResponse<PageResponseDTO<LeaveResponseDTO>> getStudentLeaves(
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Leave> leavePage = leaveRepo.findStudentLeaves(pageable);
        return buildPageResponse(leavePage, "Student leaves fetched");
    }

    // ── Get Student Pending Leaves (TEACHER) ──────────────
    public ApiResponse<PageResponseDTO<LeaveResponseDTO>> getStudentPendingLeaves(
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Leave> leavePage = leaveRepo.findStudentPendingLeaves(pageable);
        return buildPageResponse(leavePage, "Student pending leaves fetched");
    }

    // ── Get Teacher Leaves (ADMIN only) — Paginated ────────
    public ApiResponse<PageResponseDTO<LeaveResponseDTO>> getTeacherLeaves(
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Leave> leavePage = leaveRepo.findTeacherLeaves(pageable);
        return buildPageResponse(leavePage, "Teacher leaves fetched");
    }

    // ── Get Leave Statistics (ADMIN) ───────────────────────
    public ApiResponse<LeaveStatsDTO> getLeaveStats() {

        LeaveStatsDTO stats = LeaveStatsDTO.builder()
                .totalLeaves(leaveRepo.count())
                .pendingLeaves(leaveRepo.countByStatus(LeaveStatus.PENDING))
                .approvedLeaves(leaveRepo.countByStatus(LeaveStatus.APPROVED))
                .rejectedLeaves(leaveRepo.countByStatus(LeaveStatus.REJECTED))
                .autoRejectedLeaves(leaveRepo.countByStatus(LeaveStatus.AUTO_REJECTED))
                .cancelledLeaves(leaveRepo.countByStatus(LeaveStatus.CANCELLED))
                .studentLeaves(leaveRepo.countByUserRole("STUDENT"))
                .teacherLeaves(leaveRepo.countByUserRole("TEACHER"))
                .sickLeaves(leaveRepo.countByLeaveType(LeaveType.SICK))
                .personalLeaves(leaveRepo.countByLeaveType(LeaveType.PERSONAL))
                .familyLeaves(leaveRepo.countByLeaveType(LeaveType.FAMILY))
                .otherLeaves(leaveRepo.countByLeaveType(LeaveType.OTHER))
                .build();

        return ApiResponse.success("Leave statistics fetched", stats);
    }

    // ── AUTO REJECT — Runs every night at 11:59 PM ─────────
    // ✅ Your great idea implemented!
    @Scheduled(cron = "0 59 23 * * *") // Every day 11:59 PM
    @Transactional
    public void autoRejectPendingLeaves() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Leave> pendingLeaves = leaveRepo
                .findPendingLeavesForTomorrow(tomorrow);

        if (pendingLeaves.isEmpty()) {
            log.info("Auto-reject: No pending leaves for tomorrow");
            return;
        }

        pendingLeaves.forEach(leave -> {
            leave.setStatus(LeaveStatus.AUTO_REJECTED);
            leave.setAdminRemark(
                    "Auto rejected - Leave not reviewed before start date");
            leave.setReviewedAt(LocalDateTime.now());
            leaveRepo.save(leave);
            log.info("Auto rejected leave ID: {} for user: {}",
                    leave.getId(), leave.getUser().getUsername());
        });

        log.info("Auto-reject completed: {} leaves rejected", pendingLeaves.size());
    }

    // ── Page Response Builder ──────────────────────────────
    private ApiResponse<PageResponseDTO<LeaveResponseDTO>> buildPageResponse(
            Page<Leave> leavePage, String message) {

        List<LeaveResponseDTO> content = leavePage.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        PageResponseDTO<LeaveResponseDTO> response =
                PageResponseDTO.<LeaveResponseDTO>builder()
                        .content(content)
                        .pageNumber(leavePage.getNumber())
                        .pageSize(leavePage.getSize())
                        .totalElements(leavePage.getTotalElements())
                        .totalPages(leavePage.getTotalPages())
                        .isFirst(leavePage.isFirst())
                        .isLast(leavePage.isLast())
                        .hasNext(leavePage.hasNext())
                        .hasPrevious(leavePage.hasPrevious())
                        .build();

        return ApiResponse.success(message, response);
    }

    // ── Mapper ─────────────────────────────────────────────
    private LeaveResponseDTO mapToDTO(Leave l) {

        // Student info — class & section
        String className = null;
        String section = null;
        if (l.getUser().getRole() == Role.STUDENT) {
            Student student = studentRepo
                    .findByUserId(l.getUser().getId()).orElse(null);
            if (student != null) {
                className = student.getClassName();
                section = student.getSection();
            }
        }

        return LeaveResponseDTO.builder()
                .id(l.getId())
                .userId(l.getUser().getId())
                .userName(l.getUser().getFullName())
                .userRole(l.getUser().getRole().name())
                .className(className)
                .section(section)
                .leaveType(l.getLeaveType().name())
                .fromDate(l.getFromDate())
                .toDate(l.getToDate())
                .totalDays(l.getTotalDays())
                .reason(l.getReason())
                .status(l.getStatus().name())
                .adminRemark(l.getAdminRemark())
                .reviewedByName(l.getReviewedBy() != null ?
                        l.getReviewedBy().getFullName() : null)
                .reviewedAt(l.getReviewedAt())
                .createdAt(l.getCreatedAt())
                .build();
    }
}