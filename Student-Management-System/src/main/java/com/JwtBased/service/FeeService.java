package com.JwtBased.service;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.FeeDTO;
import com.JwtBased.dto.FeeReportDTO;
import com.JwtBased.entity.Fee;
import com.JwtBased.enums.FeeStatus;
import com.JwtBased.entity.Student;
import com.JwtBased.repository.FeeRepo;
import com.JwtBased.repository.StudentRepo;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeRepo feeRepo;
    private final StudentRepo studentRepo;
    private final SecurityUtils securityUtils; // ✅ Add, UserRepo hatavla

    // ── Create Fee ─────────────────────────────────────────
    @Transactional
    public ApiResponse<FeeReportDTO> createFee(FeeDTO dto) {
        Student student = studentRepo.findById(dto.getStudentId()).orElse(null);
        if (student == null) return ApiResponse.error("Student not found");

        Fee fee = Fee.builder()
                .student(student)
                .feeType(dto.getFeeType())
                .amount(dto.getAmount())
                .dueDate(dto.getDueDate())
                .status(FeeStatus.PENDING)
                .remarks(dto.getRemarks())
                .build();

        feeRepo.save(fee);
        return ApiResponse.success("Fee record created", mapToDTO(fee));
    }

    // ── Pay Fee ────────────────────────────────────────────
    @Transactional
    public ApiResponse<FeeReportDTO> payFee(
            Long feeId, String paymentMethod, String transactionId) {

        Fee fee = feeRepo.findById(feeId).orElse(null);
        if (fee == null) return ApiResponse.error("Fee record not found");
        if (fee.getStatus() == FeeStatus.PAID)
            return ApiResponse.error("Fee already paid");

        fee.setStatus(FeeStatus.PAID);
        fee.setPaidDate(LocalDate.now());
        fee.setPaymentMethod(paymentMethod);
        fee.setTransactionId(transactionId);

        feeRepo.save(fee);
        return ApiResponse.success("Fee paid successfully", mapToDTO(fee));
    }

    // ── Get All Fees ───────────────────────────────────────
    public ApiResponse<List<FeeReportDTO>> getAllFees() {
        List<FeeReportDTO> list = feeRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ApiResponse.success("All fees fetched", list);
    }

    // ── Get Student Fee Summary ────────────────────────────
    public ApiResponse<FeeReportDTO> getStudentFeeSummary(Long studentId) {
        Student student = studentRepo.findById(studentId).orElse(null);
        if (student == null) return ApiResponse.error("Student not found");

        BigDecimal paid    = feeRepo.getPaidAmountByStudent(studentId);
        BigDecimal pending = feeRepo.getPendingAmountByStudent(studentId);

        FeeReportDTO summary = FeeReportDTO.builder()
                .studentId(studentId)
                .studentName(student.getUser().getFullName())
                .rollNumber(student.getRollNumber())
                .className(student.getClassName())
                .paidAmount(paid)
                .pendingAmount(pending)
                .totalFees(paid.add(pending))
                .build();

        return ApiResponse.success("Fee summary fetched", summary);
    }

    // ── Get Pending Fees ───────────────────────────────────
    public ApiResponse<List<FeeReportDTO>> getPendingFees() {
        List<FeeReportDTO> list = feeRepo.findByStatus(FeeStatus.PENDING)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        return ApiResponse.success("Pending fees", list);
    }

    // ── Get Overdue Fees ───────────────────────────────────
    public ApiResponse<List<FeeReportDTO>> getOverdueFees() {
        feeRepo.findOverdueFees(LocalDate.now()).forEach(fee -> {
            fee.setStatus(FeeStatus.OVERDUE);
            feeRepo.save(fee);
        });
        List<FeeReportDTO> list = feeRepo.findByStatus(FeeStatus.OVERDUE)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        return ApiResponse.success("Overdue fees", list);
    }

    // ── Get Own Fees (Student) ─────────────────────────────
    // ✅ No username parameter
    public ApiResponse<FeeReportDTO> getOwnFees() {
        Long userId = securityUtils.getCurrentUserId(); // ✅ SecurityUtils madhe se

        Student student = studentRepo.findByUserId(userId).orElse(null);
        if (student == null) return ApiResponse.error("Student profile not found");

        return getStudentFeeSummary(student.getId());
    }

    // ── Mapper ─────────────────────────────────────────────
    private FeeReportDTO mapToDTO(Fee f) {
        return FeeReportDTO.builder()
                .feeId(f.getId())
                .studentId(f.getStudent().getId())
                .studentName(f.getStudent().getUser().getFullName())
                .rollNumber(f.getStudent().getRollNumber())
                .className(f.getStudent().getClassName())
                .feeType(f.getFeeType())
                .amount(f.getAmount())
                .dueDate(f.getDueDate())
                .paidDate(f.getPaidDate())
                .status(f.getStatus().name())
                .paymentMethod(f.getPaymentMethod())
                .transactionId(f.getTransactionId())
                .remarks(f.getRemarks())
                .build();
    }
}