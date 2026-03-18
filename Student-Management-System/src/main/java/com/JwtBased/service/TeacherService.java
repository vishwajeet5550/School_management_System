package com.JwtBased.service;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.PageResponseDTO;
import com.JwtBased.dto.TeacherDTO;
import com.JwtBased.dto.UserProfileDTO;
import com.JwtBased.entity.Teacher;
import com.JwtBased.entity.User;
import com.JwtBased.enums.Role;
import com.JwtBased.repository.TeacherRepo;
import com.JwtBased.repository.UserRepo;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepo userRepository;
    private final TeacherRepo teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final FileStorageService fileStorageService;

    // ── Create Teacher ─────────────────────────────────────
    @Transactional
    public ApiResponse<UserProfileDTO> createTeacher(TeacherDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername()))
            return ApiResponse.error("Username already exists");
        if (userRepository.existsByEmail(dto.getEmail()))
            return ApiResponse.error("Email already exists");

        String createdBy = securityUtils.getCurrentUsername();

        String profilePicturePath = null;
        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            try {
                profilePicturePath = fileStorageService.saveFile(
                        dto.getProfilePicture(), dto.getUsername());
            } catch (Exception e) {
                return ApiResponse.error("Profile picture upload failed: " + e.getMessage());
            }
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.TEACHER)
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .department(dto.getDepartment())
                .profilePicture(profilePicturePath)
                .isActive(true)
                .createdBy(createdBy)
                .build();
        User savedUser = userRepository.save(user);

        Teacher teacher = Teacher.builder()
                .user(savedUser)
                .subject(dto.getSubject())
                .qualification(dto.getQualification())
                .joiningDate(dto.getJoiningDate())
                .salary(dto.getSalary())
                .build();
        teacherRepository.save(teacher);

        return ApiResponse.success("Teacher created successfully",
                mapToProfile(savedUser, teacher));
    }

    // ── Get All Teachers — WITH PAGINATION ────────────────
    // ✅ Complete method with opening { and closing }
    public ApiResponse<PageResponseDTO<UserProfileDTO>> getAllTeachers(
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Teacher> teacherPage = teacherRepository.findAll(pageable);

        List<UserProfileDTO> content = teacherPage.getContent()
                .stream()
                .map(t -> mapToProfile(t.getUser(), t))
                .collect(Collectors.toList());

        PageResponseDTO<UserProfileDTO> response = PageResponseDTO
                .<UserProfileDTO>builder()
                .content(content)
                .pageNumber(teacherPage.getNumber())
                .pageSize(teacherPage.getSize())
                .totalElements(teacherPage.getTotalElements())
                .totalPages(teacherPage.getTotalPages())
                .isFirst(teacherPage.isFirst())
                .isLast(teacherPage.isLast())
                .hasNext(teacherPage.hasNext())
                .hasPrevious(teacherPage.hasPrevious()) // ✅ Fixed
                .build();

        return ApiResponse.success("Teachers fetched — Page " +
                (page + 1) + " of " + teacherPage.getTotalPages(), response);
    }

    // ── Search Teachers — WITH PAGINATION ─────────────────
    // ✅ Complete method with opening { and closing }
    public ApiResponse<PageResponseDTO<UserProfileDTO>> searchTeachers(
            String keyword, int page, int size) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTeachers(page, size, "id", "asc");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("id").ascending());

        Page<Teacher> teacherPage = teacherRepository
                .searchTeachers(keyword.trim(), pageable);

        List<UserProfileDTO> content = teacherPage.getContent()
                .stream()
                .map(t -> mapToProfile(t.getUser(), t))
                .collect(Collectors.toList());

        PageResponseDTO<UserProfileDTO> response = PageResponseDTO
                .<UserProfileDTO>builder()
                .content(content)
                .pageNumber(teacherPage.getNumber())
                .pageSize(teacherPage.getSize())
                .totalElements(teacherPage.getTotalElements())
                .totalPages(teacherPage.getTotalPages())
                .isFirst(teacherPage.isFirst())
                .isLast(teacherPage.isLast())
                .hasNext(teacherPage.hasNext())
                .hasPrevious(teacherPage.hasPrevious()) // ✅ Fixed
                .keyword(keyword)
                .build();

        return ApiResponse.success(
                teacherPage.getTotalElements() + " teacher(s) found for: " + keyword,
                response);
    }

    // ── Get Teacher By ID ──────────────────────────────────
    public ApiResponse<UserProfileDTO> getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .map(t -> ApiResponse.success("Teacher found",
                        mapToProfile(t.getUser(), t)))
                .orElse(ApiResponse.error("Teacher not found"));
    }

    // ── Update Teacher ─────────────────────────────────────
    @Transactional
    public ApiResponse<UserProfileDTO> updateTeacher(Long id, TeacherDTO dto) {
        Teacher teacher = teacherRepository.findById(id).orElse(null);
        if (teacher == null) return ApiResponse.error("Teacher not found");

        User user = teacher.getUser();

        if (dto.getFullName() != null)      user.setFullName(dto.getFullName());
        if (dto.getPhone() != null)         user.setPhone(dto.getPhone());
        if (dto.getAddress() != null)       user.setAddress(dto.getAddress());
        if (dto.getDepartment() != null)    user.setDepartment(dto.getDepartment());
        if (dto.getDateOfBirth() != null)   user.setDateOfBirth(dto.getDateOfBirth());

        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            try {
                if (user.getProfilePicture() != null) {
                    fileStorageService.deleteFile(user.getProfilePicture());
                }
                String newPath = fileStorageService.saveFile(
                        dto.getProfilePicture(), user.getUsername());
                user.setProfilePicture(newPath);
            } catch (Exception e) {
                return ApiResponse.error("Profile picture update failed: " + e.getMessage());
            }
        }

        if (dto.getSubject() != null)       teacher.setSubject(dto.getSubject());
        if (dto.getQualification() != null) teacher.setQualification(dto.getQualification());
        if (dto.getJoiningDate() != null)   teacher.setJoiningDate(dto.getJoiningDate());
        if (dto.getSalary() != null)        teacher.setSalary(dto.getSalary());

        userRepository.save(user);
        teacherRepository.save(teacher);

        return ApiResponse.success("Teacher updated", mapToProfile(user, teacher));
    }

    // ── Delete Teacher (Soft Delete) ───────────────────────
    @Transactional
    public ApiResponse<Void> deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id).orElse(null);
        if (teacher == null) return ApiResponse.error("Teacher not found");

        teacher.getUser().setIsActive(false);
        userRepository.save(teacher.getUser());
        return ApiResponse.success("Teacher deactivated", null);
    }

    // ── Search Teachers (without pagination) ───────────────
    public ApiResponse<List<UserProfileDTO>> searchTeachers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ApiResponse.success("Teachers fetched",
                    teacherRepository.findAll()
                            .stream()
                            .map(t -> mapToProfile(t.getUser(), t))
                            .collect(Collectors.toList()));
        }

        List<UserProfileDTO> list = teacherRepository
                .searchTeachers(keyword.trim())
                .stream()
                .map(t -> mapToProfile(t.getUser(), t))
                .collect(Collectors.toList());

        return ApiResponse.success(
                list.isEmpty() ? "No teachers found" :
                        list.size() + " teacher(s) found", list);
    }

    // ── Get Own Profile ────────────────────────────────────
    public ApiResponse<UserProfileDTO> getTeacherProfile() {
        Long userId = securityUtils.getCurrentUserId();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ApiResponse.error("User not found");

        Teacher teacher = teacherRepository.findByUserId(userId).orElse(null);
        return ApiResponse.success("Profile fetched", mapToProfile(user, teacher));
    }

    // ── Mapper ─────────────────────────────────────────────
    private UserProfileDTO mapToProfile(User user, Teacher teacher) {
        UserProfileDTO dto = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .department(user.getDepartment())
                .profilePicture(user.getProfilePicture())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        if (teacher != null) {
            dto.setSubject(teacher.getSubject());
            dto.setQualification(teacher.getQualification());
            dto.setJoiningDate(teacher.getJoiningDate());
            dto.setSalary(teacher.getSalary());
        }
        return dto;
    }
}