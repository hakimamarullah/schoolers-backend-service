package com.schoolers.validator;

import com.schoolers.exceptions.BadRequestException;
import com.schoolers.service.ILocalizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class FileValidatorImpl implements FileValidator {

    private ILocalizationService localizationService;
    public List<String> getAllowedContentTypes() {
        return List.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain",
                "image/jpeg", "image/png", "image/gif"
        );
    }
    public List<String> getAllowedExtensions() {
        return List.of( ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".jpg", ".jpeg", ".png", ".gif");
    }


    @Autowired
    public void setLocalizationService(ILocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    protected void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        boolean validContentType = getAllowedContentTypes().stream().anyMatch(it -> it.equalsIgnoreCase(contentType));
        if (!validContentType) {
            throw new BadRequestException(localizationService.getMessage("error.invalid-file-type", new Object[] {getAllowedContentTypesString()}));
        }
    }


    protected void validateExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BadRequestException(localizationService.getMessage("error.invalid-file-extension", new Object[] {getAllowedExtensionsString()}));
        }

        String extension = getFileExtension(filename).toLowerCase();
        boolean validExtension = getAllowedExtensions().stream().anyMatch(it -> it.equalsIgnoreCase(extension));
        if (!validExtension) {
            throw new BadRequestException(localizationService.getMessage("error.invalid-file-extension", new Object[] {getAllowedExtensionsString()}));
        }
    }

    @Override
    public void validate(MultipartFile file, Long maxFileSize) {
        validateBasicFile(file, maxFileSize);
        validateContentType(file);
        validateExtension(file);
    }

    @Override
    public String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    protected void validateBasicFile(MultipartFile file, Long maxFileSize) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(localizationService.getMessage("error.file-empty"));
        }
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException(localizationService.getMessage("error.file-size-exceeded", new Object[] {maxFileSize / 1024 / 1024}));
        }
    }

    private String getAllowedContentTypesString() {
        return String.join(", ", getAllowedContentTypes());
    }

    private String getAllowedExtensionsString() {
        return String.join(", ", getAllowedExtensions());
    }
}
