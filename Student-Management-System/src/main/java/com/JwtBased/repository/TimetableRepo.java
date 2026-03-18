package com.JwtBased.repository;

import com.JwtBased.entity.Timetable;
import com.JwtBased.enums.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepo extends JpaRepository<Timetable, Long> {

    // ── Student use karil — class timetable view ───────────
    List<Timetable> findByClassNameAndSectionAndIsActiveTrue(
            String className, String section);

    // ── Day wise timetable ─────────────────────────────────
    List<Timetable> findByClassNameAndSectionAndDayAndIsActiveTrue(
            String className, String section, Day day);

    // ── Teacher use karil — own schedule view ─────────────
    List<Timetable> findByTeacherIdAndIsActiveTrue(Long teacherId);

    // ── Teacher day wise schedule ──────────────────────────
    List<Timetable> findByTeacherIdAndDayAndIsActiveTrue(Long teacherId, Day day);

    // ── Check conflict — same class, section, day, time ───
    boolean existsByClassNameAndSectionAndDayAndTimeSlotAndIsActiveTrue(
            String className, String section, Day day, String timeSlot);

    // ── Check teacher conflict — same teacher, day, time ──
    boolean existsByTeacherIdAndDayAndTimeSlotAndIsActiveTrue(
            Long teacherId, Day day, String timeSlot);

    // ── Admin — all timetables ─────────────────────────────
    List<Timetable> findByIsActiveTrue();

    // ── Class wise full timetable ──────────────────────────
    @Query("SELECT t FROM Timetable t WHERE " +
            "t.className = :className AND " +
            "t.isActive = true " +
            "ORDER BY t.day, t.timeSlot")
    List<Timetable> findByClassNameOrderByDayAndTime(
            @Param("className") String className);
}