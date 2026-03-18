package com.JwtBased.service;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.AttendanceDTO;
import com.JwtBased.dto.AttendanceReportDTO;
import com.JwtBased.entity.Attendance;
import com.JwtBased.enums.AttendanceStatus;
import com.JwtBased.entity.Student;
import com.JwtBased.entity.User;
import com.JwtBased.repository.AttendanceRepo;
import com.JwtBased.repository.StudentRepo;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepo attendanceRepo;
    private final StudentRepo studentRepo;
    private final SecurityUtils securityUtils; // ✅ Add, UserRepo hatavla

    // ── Mark Attendance ────────────────────────────────────
    @Transactional
    public ApiResponse<AttendanceReportDTO> markAttendance(AttendanceDTO dto) {
        // ✅ SecurityUtils madhe se directly
        User currentUser = securityUtils.getCurrentUser();

        Student student = studentRepo.findById(dto.getStudentId()).orElse(null);
        if (student == null) return ApiResponse.error("Student not found");

        if (attendanceRepo.existsByStudentIdAndDate(dto.getStudentId(), dto.getDate()))
            return ApiResponse.error("Attendance already marked for this date");

        Attendance attendance = Attendance.builder()
                .student(student)
                .markedBy(currentUser) // ✅ SecurityUtils madhe se
                .date(dto.getDate())
                .status(AttendanceStatus.valueOf(dto.getStatus().toUpperCase())) // ✅ toUpperCase fix
                .remarks(dto.getRemarks())
                .build();

        attendanceRepo.save(attendance);
        return ApiResponse.success("Attendance marked", mapToDTO(attendance));
    }

    // ── Update Attendance ──────────────────────────────────
    @Transactional
    public ApiResponse<AttendanceReportDTO> updateAttendance(Long id, AttendanceDTO dto) {
        Attendance attendance = attendanceRepo.findById(id).orElse(null);
        if (attendance == null) return ApiResponse.error("Attendance record not found");

        attendance.setStatus(AttendanceStatus.valueOf(dto.getStatus().toUpperCase())); // ✅ toUpperCase fix
        attendance.setRemarks(dto.getRemarks());
        attendanceRepo.save(attendance);

        return ApiResponse.success("Attendance updated", mapToDTO(attendance));
    }

    // ── Get Attendance By Class And Date ───────────────────
    public ApiResponse<List<AttendanceReportDTO>> getAttendanceByClassAndDate(
            String className, String section, LocalDate date) {

        List<AttendanceReportDTO> list = attendanceRepo
                .findByClassAndDate(className, section, date)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ApiResponse.success("Attendance fetched", list);
    }

    // ── Get Student Attendance Report ──────────────────────
    public ApiResponse<AttendanceReportDTO> getStudentAttendanceReport(Long studentId) {
        Student student = studentRepo.findById(studentId).orElse(null);
        if (student == null) return ApiResponse.error("Student not found");

        long total    = attendanceRepo.countByStudentId(studentId);
        long present  = attendanceRepo.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long absent   = attendanceRepo.countByStudentIdAndStatus(studentId, AttendanceStatus.ABSENT);
        long late     = attendanceRepo.countByStudentIdAndStatus(studentId, AttendanceStatus.LATE);
        double percentage = total > 0 ? (double)(present + late) / total * 100 : 0;

        AttendanceReportDTO report = AttendanceReportDTO.builder()
                .studentId(studentId)
                .studentName(student.getUser().getFullName())
                .rollNumber(student.getRollNumber())
                .className(student.getClassName())
                .section(student.getSection())
                .totalDays(total)
                .presentDays(present)
                .absentDays(absent)
                .lateDays(late)
                .attendancePercentage(Math.round(percentage * 100.0) / 100.0)
                .build();

        return ApiResponse.success("Attendance report fetched", report);
    }

    // ── Get Student Attendance By Date Range ───────────────
    public ApiResponse<List<AttendanceReportDTO>> getStudentAttendanceByDateRange(
            Long studentId, LocalDate startDate, LocalDate endDate) {

        List<AttendanceReportDTO> list = attendanceRepo
                .findByStudentIdAndDateBetween(studentId, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ApiResponse.success("Attendance fetched", list);
    }

    // ── Get Own Attendance (Student) ───────────────────────
    // ✅ No username parameter
    public ApiResponse<AttendanceReportDTO> getOwnAttendance() {
        Long userId = securityUtils.getCurrentUserId();

        Student student = studentRepo.findByUserId(userId).orElse(null);
        if (student == null) return ApiResponse.error("Student profile not found");

        return getStudentAttendanceReport(student.getId());
    }

    // ── Mapper ─────────────────────────────────────────────
    private AttendanceReportDTO mapToDTO(Attendance a) {
        return AttendanceReportDTO.builder()
                .studentId(a.getStudent().getId())
                .studentName(a.getStudent().getUser().getFullName())
                .rollNumber(a.getStudent().getRollNumber())
                .className(a.getStudent().getClassName())
                .section(a.getStudent().getSection())
                .date(a.getDate())
                .status(a.getStatus().name())
                .remarks(a.getRemarks())
                .markedBy(a.getMarkedBy() != null ?
                        a.getMarkedBy().getFullName() : "N/A")
                .build();
    }
}