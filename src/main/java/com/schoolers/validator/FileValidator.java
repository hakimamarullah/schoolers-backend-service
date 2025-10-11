package com.schoolers.validator;

import org.springframework.web.multipart.MultipartFile;

public interface FileValidator {
    void validate(MultipartFile file, Long maxFileSize);

    String getFileExtension(String originalFilename);
}

