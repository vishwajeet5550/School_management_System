package com.JwtBased.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    // ── Save File to Local Disk ────────────────────────────
    public String saveFile(MultipartFile file, String username) throws IOException {

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed!");
        }

        // Validate file size (5MB max)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 5MB!");
        }

        // Create directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        // e.g. rohit_student_a1b2c3d4.jpg
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = username + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // Save file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path to store in DB
        return "/uploads/profiles/" + newFilename;
    }

    // ── Delete Old File ────────────────────────────────────
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        try {
            // /uploads/profiles/filename.jpg → C:/StudentManagement/uploads/profiles/filename.jpg
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            Path path = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Could not delete file: " + filePath);
        }
    }
}