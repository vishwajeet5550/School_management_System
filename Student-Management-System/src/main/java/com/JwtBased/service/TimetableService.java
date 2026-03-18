package com.JwtBased.service;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.TimetableDTO;
import com.JwtBased.dto.TimetableResponseDTO;
import com.JwtBased.entity.Timetable;
import com.JwtBased.enums.Day;
import com.JwtBased.entity.User;
import com.JwtBased.entity.Student;
import com.JwtBased.repository.StudentRepo;
import com.JwtBased.repository.TimetableRepo;
import com.JwtBased.repository.UserRepo;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableRepo timetableRepo;
    private final UserRepo userRepository;
    private final StudentRepo studentRepo;
    private final SecurityUtils securityUtils;

    // ── Create Timetable (ADMIN) ───────────────────────────
    @Transactional
    public ApiResponse<TimetableResponseDTO> createTimetable(TimetableDTO dto) {

        // Teacher exists ka check
        User teacher = userRepository.findById(dto.getTeacherId()).orElse(null);
        if (teacher == null)
            return ApiResponse.error("Teacher not found");

        // Class conflict check — same class, section, day, time already ahe ka
        if (timetableRepo.existsByClassNameAndSectionAndDayAndTimeSlotAndIsActiveTrue(
                dto.getClassName(), dto.getSection(),
                Day.valueOf(dto.getDay().toUpperCase()), dto.getTimeSlot()))
            return ApiResponse.error(
                    "Timetable conflict! " + dto.getClassName() + "-" + dto.getSection() +
                            " already has a class on " + dto.getDay() + " at " + dto.getTimeSlot());

        // Teacher conflict check — same teacher, day, time already busy ka
        if (timetableRepo.existsByTeacherIdAndDayAndTimeSlotAndIsActiveTrue(
                dto.getTeacherId(),
                Day.valueOf(dto.getDay().toUpperCase()), dto.getTimeSlot()))
            return ApiResponse.error(
                    "Teacher conflict! " + teacher.getFullName() +
                            " is already assigned on " + dto.getDay() + " at " + dto.getTimeSlot());

        Timetable timetable = Timetable.builder()
                .className(dto.getClassName())
                .section(dto.getSection())
                .subject(dto.getSubject())
                .teacher(teacher)
                .day(Day.valueOf(dto.getDay().toUpperCase()))
                .timeSlot(dto.getTimeSlot())
                .room(dto.getRoom())
                .isActive(true)
                .build();

        timetableRepo.save(timetable);
        return ApiResponse.success("Timetable created successfully", mapToDTO(timetable));
    }

    // ── Update Timetable (ADMIN) ───────────────────────────
    @Transactional
    public ApiResponse<TimetableResponseDTO> updateTimetable(Long id, TimetableDTO dto) {
        Timetable timetable = timetableRepo.findById(id).orElse(null);
        if (timetable == null)
            return ApiResponse.error("Timetable entry not found");

        // Teacher update
        if (dto.getTeacherId() != null) {
            User teacher = userRepository.findById(dto.getTeacherId()).orElse(null);
            if (teacher == null) return ApiResponse.error("Teacher not found");
            timetable.setTeacher(teacher);
        }

        if (dto.getSubject() != null)   timetable.setSubject(dto.getSubject());
        if (dto.getTimeSlot() != null)  timetable.setTimeSlot(dto.getTimeSlot());
        if (dto.getRoom() != null)      timetable.setRoom(dto.getRoom());
        if (dto.getDay() != null)
            timetable.setDay(Day.valueOf(dto.getDay().toUpperCase()));

        timetableRepo.save(timetable);
        return ApiResponse.success("Timetable updated", mapToDTO(timetable));
    }

    // ── Delete Timetable (ADMIN) — Soft Delete ─────────────
    @Transactional
    public ApiResponse<Void> deleteTimetable(Long id) {
        Timetable timetable = timetableRepo.findById(id).orElse(null);
        if (timetable == null)
            return ApiResponse.error("Timetable entry not found");

        timetable.setIsActive(false);
        timetableRepo.save(timetable);
        return ApiResponse.success("Timetable entry deleted", null);
    }

    // ── Get All Timetables (ADMIN) ─────────────────────────
    public ApiResponse<List<TimetableResponseDTO>> getAllTimetables() {
        List<TimetableResponseDTO> list = timetableRepo.findByIsActiveTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ApiResponse.success("All timetables fetched", list);
    }

    // ── Get Timetable By ID (ADMIN) ────────────────────────
    public ApiResponse<TimetableResponseDTO> getTimetableById(Long id) {
        return timetableRepo.findById(id)
                .map(t -> ApiResponse.success("Timetable found", mapToDTO(t)))
                .orElse(ApiResponse.error("Timetable not found"));
    }

    // ── Get Class Timetable (STUDENT) ──────────────────────
    public ApiResponse<List<TimetableResponseDTO>> getClassTimetable(
            String className, String section) {

        List<TimetableResponseDTO> list = timetableRepo
                .findByClassNameAndSectionAndIsActiveTrue(className, section)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        if (list.isEmpty())
            return ApiResponse.error("No timetable found for " +
                    className + "-" + section);

        return ApiResponse.success("Class timetable fetched", list);
    }
    // ── Get Own Timetable By Day (STUDENT) ────────────────
    public ApiResponse<List<TimetableResponseDTO>> getOwnTimetableByDay(String day) {
        Long userId = securityUtils.getCurrentUserId();

        Student student = studentRepo.findByUserId(userId).orElse(null);
        if (student == null)
            return ApiResponse.error("Student profile not found");

        try {
            // ✅ Day validate karo
            Day dayEnum = Day.valueOf(day.toUpperCase());

            List<TimetableResponseDTO> list = timetableRepo
                    .findByClassNameAndSectionAndDayAndIsActiveTrue(
                            student.getClassName(),
                            student.getSection(),
                            dayEnum)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            if (list.isEmpty())
                return ApiResponse.error("No timetable found for " + day);

            return ApiResponse.success(day + " timetable fetched", list);

        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Invalid day! Use: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY");
        }
    }

    // ── Get Class Timetable By Day (STUDENT) ───────────────
    public ApiResponse<List<TimetableResponseDTO>> getClassTimetableByDay(
            String className, String section, String day) {

        List<TimetableResponseDTO> list = timetableRepo
                .findByClassNameAndSectionAndDayAndIsActiveTrue(
                        className, section, Day.valueOf(day.toUpperCase()))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ApiResponse.success("Timetable for " + day + " fetched", list);
    }

    // ── Get Own Timetable (STUDENT) ────────────────────────
    public ApiResponse<List<TimetableResponseDTO>> getOwnClassTimetable() {
        Long userId = securityUtils.getCurrentUserId();

        Student student = studentRepo.findByUserId(userId).orElse(null);
        if (student == null)
            return ApiResponse.error("Student profile not found");

        return getClassTimetable(student.getClassName(), student.getSection());
    }

    // ── Get Own Schedule (TEACHER) ─────────────────────────
    public ApiResponse<List<TimetableResponseDTO>> getOwnSchedule() {
        Long userId = securityUtils.getCurrentUserId();

        List<TimetableResponseDTO> list = timetableRepo
                .findByTeacherIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        if (list.isEmpty())
            return ApiResponse.error("No schedule found for this teacher");

        return ApiResponse.success("Teacher schedule fetched", list);
    }

    // ── Get Teacher Schedule By Day (TEACHER) ──────────────
    public ApiResponse<List<TimetableResponseDTO>> getOwnScheduleByDay(String day) {
        Long userId = securityUtils.getCurrentUserId();

        List<TimetableResponseDTO> list = timetableRepo
                .findByTeacherIdAndDayAndIsActiveTrue(
                        userId, Day.valueOf(day.toUpperCase()))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ApiResponse.success("Schedule for " + day + " fetched", list);
    }

    // ── Mapper ─────────────────────────────────────────────
    private TimetableResponseDTO mapToDTO(Timetable t) {
        return TimetableResponseDTO.builder()
                .id(t.getId())
                .className(t.getClassName())
                .section(t.getSection())
                .subject(t.getSubject())
                .teacherId(t.getTeacher().getId())
                .teacherName(t.getTeacher().getFullName())
                .teacherPhone(t.getTeacher().getPhone())
                .day(t.getDay().name())
                .timeSlot(t.getTimeSlot())
                .room(t.getRoom())
                .isActive(t.getIsActive())
                .build();
    }
}