package com.schoolers.service.impl;

import com.schoolers.service.IFileStorageService;
import com.schoolers.validator.FileValidator;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService implements IFileStorageService {

    private final FileValidator fileValidator;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-image-size:5242880}") // 5MB
    private Long maxImageSize;

    @Value("${file.max-document-size:26214400}") // 25MB
    private Long maxDocumentSize;

    @Override
    public String storeProfilePicture(MultipartFile file, String userType, String identifier) {
        fileValidator.validate(file, maxImageSize);
        return storeFile(file, "profile-pictures/" + userType, identifier);
    }

    @Override
    public String storeAssignmentResource(MultipartFile file, Long assignmentId) {
        fileValidator.validate(file, maxDocumentSize);
        return storeFile(file, "assignment-resources", "assignment_" + assignmentId);
    }



    private String storeFile(MultipartFile file, String subDirectory, String identifier) {
        try {
            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = fileValidator.getFileExtension(originalFilename);
            String filename = identifier + "_" + UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String url = "/" + uploadDir + "/" + subDirectory + "/" + filename;
            log.info("File stored: {}", url);

            return url;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(fileUrl.substring(1));
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileUrl);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

}
