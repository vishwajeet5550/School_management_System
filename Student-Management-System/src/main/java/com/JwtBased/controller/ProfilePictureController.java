package com.JwtBased.controller;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.entity.User;
import com.JwtBased.repository.UserRepo;
import com.JwtBased.service.FileStorageService;
import com.JwtBased.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Profile Picture", description = "Upload and view profile pictures")
public class ProfilePictureController {

    private final FileStorageService fileStorageService;
    private final UserRepo userRepository;
    private final SecurityUtils securityUtils;

    @Value("${file.upload.dir}")
    private String uploadDir;

    // ── Upload Profile Picture (Student) ───────────────────
    @PostMapping(value = "/student/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student - Upload own profile picture")
    public ResponseEntity<ApiResponse<String>> uploadStudentPicture(
            // ✅ @RequestPart use karo — Swagger automatically file picker show karto
            @RequestPart("file") MultipartFile file) {
        try {
            User user = securityUtils.getCurrentUser();

            if (user.getProfilePicture() != null)
                fileStorageService.deleteFile(user.getProfilePicture());

            String filePath = fileStorageService.saveFile(file, user.getUsername());
            user.setProfilePicture(filePath);
            userRepository.save(user);

            return ResponseEntity.ok(
                    ApiResponse.success("Profile picture uploaded successfully", filePath));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to upload: " + e.getMessage()));
        }
    }

    // ── Upload Profile Picture (Teacher) ───────────────────
    @PostMapping(value = "/teacher/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Teacher - Upload own profile picture")
    public ResponseEntity<ApiResponse<String>> uploadTeacherPicture(
            @RequestPart("file") MultipartFile file) {
        try {
            User user = securityUtils.getCurrentUser();

            if (user.getProfilePicture() != null)
                fileStorageService.deleteFile(user.getProfilePicture());

            String filePath = fileStorageService.saveFile(file, user.getUsername());
            user.setProfilePicture(filePath);
            userRepository.save(user);

            return ResponseEntity.ok(
                    ApiResponse.success("Profile picture uploaded successfully", filePath));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to upload: " + e.getMessage()));
        }
    }

    // ── Admin Upload For Any User ──────────────────────────
    @PostMapping(value = "/admin/picture/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin - Upload profile picture for any user")
    public ResponseEntity<ApiResponse<String>> adminUploadPicture(
            @PathVariable Long userId,
            @RequestPart("file") MultipartFile file) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not found with id: " + userId));

            if (user.getProfilePicture() != null)
                fileStorageService.deleteFile(user.getProfilePicture());

            String filePath = fileStorageService.saveFile(file, user.getUsername());
            user.setProfilePicture(filePath);
            userRepository.save(user);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Profile picture uploaded for: " + user.getUsername(), filePath));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to upload: " + e.getMessage()));
        }
    }

    // ── View Profile Picture ───────────────────────────────
    @GetMapping("/picture/{filename}")
    @Operation(summary = "View profile picture by filename")
    public ResponseEntity<Resource> viewPicture(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) return ResponseEntity.notFound().build();

            String contentType = "image/jpeg";
            if (filename.endsWith(".png"))  contentType = "image/png";
            if (filename.endsWith(".gif"))  contentType = "image/gif";
            if (filename.endsWith(".webp")) contentType = "image/webp";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Delete Own Picture ─────────────────────────────────
    @DeleteMapping("/picture")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Delete own profile picture")
    public ResponseEntity<ApiResponse<String>> deletePicture() {
        User user = securityUtils.getCurrentUser();

        if (user.getProfilePicture() == null) return ResponseEntity.badRequest()
                .body(ApiResponse.error("No profile picture found"));

        fileStorageService.deleteFile(user.getProfilePicture());
        user.setProfilePicture(null);
        userRepository.save(user);

        return ResponseEntity.ok(
                ApiResponse.success("Profile picture deleted successfully", null));
    }
}