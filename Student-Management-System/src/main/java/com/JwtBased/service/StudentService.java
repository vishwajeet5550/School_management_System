package com.JwtBased.service;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.PageResponseDTO;
import com.JwtBased.dto.StudentDTO;
import com.JwtBased.dto.UserProfileDTO;
import com.JwtBased.entity.Student;
import com.JwtBased.entity.User;
import com.JwtBased.enums.Role;
import com.JwtBased.repository.StudentRepo;
import com.JwtBased.repository.UserRepo;
import com.JwtBased.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
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
public class StudentService {

    private final UserRepo userRepository;
    private final StudentRepo studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final FileStorageService fileStorageService;

    // ── Create Student ─────────────────────────────────────
    @Transactional
    public ApiResponse<UserProfileDTO> createStudent(StudentDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername()))
            return ApiResponse.error("Username already exists");
        if (userRepository.existsByEmail(dto.getEmail()))
            return ApiResponse.error("Email already exists");
        if (studentRepository.existsByRollNumber(dto.getRollNumber()))
            return ApiResponse.error("Roll number already exists");

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
                .role(Role.STUDENT)
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .dateOfBirth(dto.getDateOfBirth())
                .profilePicture(profilePicturePath)
                .isActive(true)
                .createdBy(createdBy)
                .build();
        User savedUser = userRepository.save(user);

        Student student = Student.builder()
                .user(savedUser)
                .rollNumber(dto.getRollNumber())
                .className(dto.getClassName())
                .section(dto.getSection())
                .parentName(dto.getParentName())
                .parentPhone(dto.getParentPhone())
                .admissionDate(dto.getAdmissionDate())
                .build();
        studentRepository.save(student);

        return ApiResponse.success("Student created successfully",
                mapToProfile(savedUser, student));
    }

    // ── Get All Students — WITH PAGINATION ────────────────
    public ApiResponse<PageResponseDTO<UserProfileDTO>> getAllStudents(
            int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Student> studentPage = studentRepository.findAll(pageable);

        List<UserProfileDTO> content = studentPage.getContent()
                .stream()
                .map(s -> mapToProfile(s.getUser(), s))
                .collect(Collectors.toList());

        PageResponseDTO<UserProfileDTO> response = PageResponseDTO
                .<UserProfileDTO>builder()
                .content(content)
                .pageNumber(studentPage.getNumber())
                .pageSize(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .isFirst(studentPage.isFirst())
                .isLast(studentPage.isLast())
                .hasNext(studentPage.hasNext())
                .hasPrevious(studentPage.hasPrevious()) // ✅ Fix — hasPreviousPage() → hasPrevious()
                .build();

        return ApiResponse.success("Students fetched — Page " +
                (page + 1) + " of " + studentPage.getTotalPages(), response);
    }

    // ── Search Students — WITH PAGINATION ─────────────────
    public ApiResponse<PageResponseDTO<UserProfileDTO>> searchStudents(
            String keyword, int page, int size) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents(page, size, "id", "asc");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("id").ascending());

        Page<Student> studentPage = studentRepository
                .searchStudents(keyword.trim(), pageable);

        List<UserProfileDTO> content = studentPage.getContent()
                .stream()
                .map(s -> mapToProfile(s.getUser(), s))
                .collect(Collectors.toList());

        PageResponseDTO<UserProfileDTO> response = PageResponseDTO
                .<UserProfileDTO>builder()
                .content(content)
                .pageNumber(studentPage.getNumber())
                .pageSize(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .isFirst(studentPage.isFirst())
                .isLast(studentPage.isLast())
                .hasNext(studentPage.hasNext())
                .hasPrevious(studentPage.hasPrevious()) // ✅ Fix
                .keyword(keyword)
                .build();

        return ApiResponse.success(
                studentPage.getTotalElements() + " student(s) found for: " + keyword,
                response);
    }

    // ── Get Student By ID ──────────────────────────────────
    public ApiResponse<UserProfileDTO> getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(s -> ApiResponse.success("Student found",
                        mapToProfile(s.getUser(), s)))
                .orElse(ApiResponse.error("Student not found"));
    }

    // ── Update Student ─────────────────────────────────────
    @Transactional
    public ApiResponse<UserProfileDTO> updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return ApiResponse.error("Student not found");

        User user = student.getUser();

        if (dto.getFullName() != null)      user.setFullName(dto.getFullName());
        if (dto.getPhone() != null)         user.setPhone(dto.getPhone());
        if (dto.getAddress() != null)       user.setAddress(dto.getAddress());
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

        if (dto.getRollNumber() != null)    student.setRollNumber(dto.getRollNumber());
        if (dto.getClassName() != null)     student.setClassName(dto.getClassName());
        if (dto.getSection() != null)       student.setSection(dto.getSection());
        if (dto.getParentName() != null)    student.setParentName(dto.getParentName());
        if (dto.getParentPhone() != null)   student.setParentPhone(dto.getParentPhone());
        if (dto.getAdmissionDate() != null) student.setAdmissionDate(dto.getAdmissionDate());

        userRepository.save(user);
        studentRepository.save(student);

        return ApiResponse.success("Student updated", mapToProfile(user, student));
    }

    // ── Delete Student ─────────────────────────────────────
    @Transactional
    public ApiResponse<Void> deleteStudent(Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return ApiResponse.error("Student not found");

        student.getUser().setIsActive(false);
        userRepository.save(student.getUser());
        return ApiResponse.success("Student deactivated", null);
    }

    // ── Get Own Profile ────────────────────────────────────
    public ApiResponse<UserProfileDTO> getStudentProfile() {
        Long userId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ApiResponse.error("User not found");

        Student student = studentRepository.findByUserId(userId).orElse(null);
        return ApiResponse.success("Profile fetched", mapToProfile(user, student));
    }

    // ── Mapper ─────────────────────────────────────────────
    private UserProfileDTO mapToProfile(User user, Student student) {
        UserProfileDTO dto = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .profilePicture(user.getProfilePicture())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        if (student != null) {
            dto.setRollNumber(student.getRollNumber());
            dto.setClassName(student.getClassName());
            dto.setSection(student.getSection());
            dto.setParentName(student.getParentName());
            dto.setParentPhone(student.getParentPhone());
            dto.setAdmissionDate(student.getAdmissionDate());
        }
        return dto;
    }
}