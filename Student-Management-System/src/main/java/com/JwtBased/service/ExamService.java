package com.JwtBased.service;

import com.JwtBased.dto.*;
import com.JwtBased.entity.*;
import com.JwtBased.repository.*;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepo examRepo;
    private final MarkRepo markRepo;
    private final StudentRepo studentRepo;
    private final SecurityUtils securityUtils; // ✅ Add, UserRepo hatavla

    // ── Create Exam ────────────────────────────────────────
    @Transactional
    // ✅ No createdByUsername parameter
    public ApiResponse<ExamDTO> createExam(ExamDTO dto) {
        User currentUser = securityUtils.getCurrentUser(); // ✅ SecurityUtils madhe se

        Exam exam = Exam.builder()
                .examName(dto.getExamName())
                .subject(dto.getSubject())
                .className(dto.getClassName())
                .section(dto.getSection())
                .examDate(dto.getExamDate())
                .totalMarks(dto.getTotalMarks())
                .passingMarks(dto.getPassingMarks())
                .createdBy(currentUser) // ✅ SecurityUtils madhe se
                .build();

        examRepo.save(exam);
        return ApiResponse.success("Exam created", dto);
    }

    // ── Get All Exams ──────────────────────────────────────
    public ApiResponse<List<Exam>> getAllExams() {
        return ApiResponse.success("Exams fetched", examRepo.findAll());
    }

    // ── Get Exams By Class ─────────────────────────────────
    public ApiResponse<List<Exam>> getExamsByClass(String className) {
        return ApiResponse.success("Exams fetched", examRepo.findByClassName(className));
    }

    // ── Add Marks ──────────────────────────────────────────
    @Transactional
    public ApiResponse<MarkReportDTO> addMarks(MarkDTO dto) {
        Student student = studentRepo.findById(dto.getStudentId()).orElse(null);
        if (student == null) return ApiResponse.error("Student not found");

        Exam exam = examRepo.findById(dto.getExamId()).orElse(null);
        if (exam == null) return ApiResponse.error("Exam not found");

        if (markRepo.existsByStudentIdAndExamId(dto.getStudentId(), dto.getExamId()))
            return ApiResponse.error("Marks already added for this student and exam");

        if (dto.getObtainedMarks() > exam.getTotalMarks())
            return ApiResponse.error("Obtained marks cannot exceed total marks: " + exam.getTotalMarks());

        boolean isPassed = dto.getObtainedMarks() >= exam.getPassingMarks();
        String grade = calculateGrade(dto.getObtainedMarks(), exam.getTotalMarks(), exam.getPassingMarks());

        Mark mark = Mark.builder()
                .student(student)
                .exam(exam)
                .obtainedMarks(dto.getObtainedMarks())
                .grade(grade)
                .isPassed(isPassed)
                .remarks(dto.getRemarks())
                .build();

        markRepo.save(mark);
        return ApiResponse.success("Marks added", mapToMarkReport(mark));
    }

    // ── Update Marks ───────────────────────────────────────
    @Transactional
    public ApiResponse<MarkReportDTO> updateMarks(Long markId, MarkDTO dto) {
        Mark mark = markRepo.findById(markId).orElse(null);
        if (mark == null) return ApiResponse.error("Mark record not found");

        Exam exam = mark.getExam();

        if (dto.getObtainedMarks() > exam.getTotalMarks())
            return ApiResponse.error("Obtained marks cannot exceed: " + exam.getTotalMarks());

        boolean isPassed = dto.getObtainedMarks() >= exam.getPassingMarks();
        String grade = calculateGrade(dto.getObtainedMarks(), exam.getTotalMarks(), exam.getPassingMarks());

        mark.setObtainedMarks(dto.getObtainedMarks());
        mark.setGrade(grade);
        mark.setIsPassed(isPassed);
        mark.setRemarks(dto.getRemarks());

        markRepo.save(mark);
        return ApiResponse.success("Marks updated", mapToMarkReport(mark));
    }

    // ── Get Student Results ────────────────────────────────
    public ApiResponse<List<MarkReportDTO>> getStudentResults(Long studentId) {
        List<MarkReportDTO> list = markRepo.findByStudentId(studentId)
                .stream()
                .map(this::mapToMarkReport)
                .collect(Collectors.toList());
        return ApiResponse.success("Results fetched", list);
    }

    // ── Get Own Results (Student) ──────────────────────────
    // ✅ No username parameter
    public ApiResponse<List<MarkReportDTO>> getOwnResults() {
        Long userId = securityUtils.getCurrentUserId(); // ✅ SecurityUtils madhe se

        Student student = studentRepo.findByUserId(userId).orElse(null);
        if (student == null) return ApiResponse.error("Student profile not found");

        return getStudentResults(student.getId());
    }

    // ── Get Exam Results ───────────────────────────────────
    public ApiResponse<List<MarkReportDTO>> getExamResults(Long examId) {
        List<MarkReportDTO> list = markRepo.findByExamId(examId)
                .stream()
                .map(this::mapToMarkReport)
                .collect(Collectors.toList());
        return ApiResponse.success("Exam results fetched", list);
    }

    // ── Get Toppers ────────────────────────────────────────
    public ApiResponse<List<MarkReportDTO>> getToppers(Long examId) {
        List<MarkReportDTO> list = markRepo.findToppersByExam(examId)
                .stream()
                .map(this::mapToMarkReport)
                .collect(Collectors.toList());
        return ApiResponse.success("Toppers fetched", list);
    }

    // ── Grade Calculator ───────────────────────────────────
    private String calculateGrade(int obtained, int total, int passingMarks) {
        if (obtained < passingMarks) return "F";
        double percent = (double) obtained / total * 100;
        if (percent >= 90) return "A+";
        if (percent >= 80) return "A";
        if (percent >= 70) return "B";
        if (percent >= 60) return "C";
        if (percent >= 50) return "D";
        return "F";
    }

    // ── Mapper ─────────────────────────────────────────────
    private MarkReportDTO mapToMarkReport(Mark m) {
        return MarkReportDTO.builder()
                .markId(m.getId())
                .studentId(m.getStudent().getId())
                .studentName(m.getStudent().getUser().getFullName())
                .rollNumber(m.getStudent().getRollNumber())
                .examName(m.getExam().getExamName())
                .subject(m.getExam().getSubject())
                .className(m.getExam().getClassName())
                .examDate(m.getExam().getExamDate())
                .totalMarks(m.getExam().getTotalMarks())
                .obtainedMarks(m.getObtainedMarks())
                .passingMarks(m.getExam().getPassingMarks())
                .grade(m.getGrade())
                .isPassed(m.getIsPassed())
                .remarks(m.getRemarks())
                .build();
    }
}