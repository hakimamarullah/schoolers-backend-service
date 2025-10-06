package com.schoolers.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {

    String storeProfilePicture(MultipartFile file, String userType, String identifier);

    void deleteProfilePicture(String fileUri);

}
