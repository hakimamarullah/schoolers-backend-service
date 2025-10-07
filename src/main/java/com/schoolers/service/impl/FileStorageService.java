package com.schoolers.service.impl;

import com.schoolers.exceptions.BadRequestException;
import com.schoolers.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService implements IFileStorageService {

    @Value("${file.upload-dir:uploads/profile-pictures}")
    private String uploadDir;

    @Value("${file.max-size:5242880}") // 5MB default
    private Long maxFileSize;

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/png", "image/gif"
    };

    /**
     * Store profile picture and return URL
     */
    @Override
    public String storeProfilePicture(MultipartFile file, String userType, String identifier) {
        validateFile(file);

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir, userType);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = identifier + "_" + UUID.randomUUID().toString() + extension;

            // Store file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL
            String url = "/" + uploadDir + "/" + userType + "/" + filename;
            log.info("Profile picture stored: {}", url);

            return url;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new IllegalStateException("Failed to store profile picture", e);
        }
    }

    /**
     * Delete profile picture
     */
    @Override
    public void deleteProfilePicture(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(fileUrl.substring(1)); // Remove leading /
            Files.deleteIfExists(filePath);
            log.info("Profile picture deleted: {}", fileUrl);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    /**
     * Validate file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum limit of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Check content type
        String contentType = file.getContentType();
        boolean validContentType = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                validContentType = true;
                break;
            }
        }
        if (!validContentType) {
            throw new BadRequestException("Invalid file type. Only JPG, PNG, and GIF are allowed");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BadRequestException("Invalid filename");
        }

        String extension = getFileExtension(filename).toLowerCase();
        boolean validExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                validExtension = true;
                break;
            }
        }
        if (!validExtension) {
            throw new BadRequestException("Invalid file extension. Only .jpg, .jpeg, .png, .gif are allowed");
        }
    }

    private String getFileExtension(String filename) {
        if (Objects.isNull(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
