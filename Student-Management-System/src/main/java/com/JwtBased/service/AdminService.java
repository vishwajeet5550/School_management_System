package com.JwtBased.service;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.DashboardStats;
import com.JwtBased.dto.UserProfileDTO;
import com.JwtBased.entity.User;
import com.JwtBased.enums.Role;
import com.JwtBased.repository.StudentRepo;
import com.JwtBased.repository.TeacherRepo;
import com.JwtBased.repository.UserRepo;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepo userRepository;
    private final StudentRepo studentRepository;
    private final TeacherRepo teacherRepository;
    private final SecurityUtils securityUtils; // ✅ Add

    public ApiResponse<DashboardStats> getDashboardStats() {
        DashboardStats stats = DashboardStats.builder()
                .totalStudents(userRepository.countByRole(Role.STUDENT))
                .totalTeachers(userRepository.countByRole(Role.TEACHER))
                .totalAdmins(userRepository.countByRole(Role.ADMIN))
                .activeStudents(userRepository.countByRoleAndIsActive(Role.STUDENT, true))
                .activeTeachers(userRepository.countByRoleAndIsActive(Role.TEACHER, true))
                .build();
        return ApiResponse.success("Dashboard stats fetched", stats);
    }

    // ✅ No username parameter — SecurityUtils se directly
    public ApiResponse<UserProfileDTO> getAdminProfile() {
        User user = securityUtils.getCurrentUser();

        UserProfileDTO dto = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        return ApiResponse.success("Admin profile fetched", dto);
    }
}