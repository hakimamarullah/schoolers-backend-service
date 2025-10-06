package com.schoolers.repository;

import com.schoolers.models.BiometricCredential;
import com.schoolers.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BiometricCredentialRepository extends JpaRepository<BiometricCredential, Long> {
    Optional<BiometricCredential> findByPublicKeyHash(String publicKeyHash);
    List<BiometricCredential> findByUserAndActiveTrue(User user);
    Optional<BiometricCredential> findByUserAndDeviceIdAndActiveTrue(User user, String deviceId);
}
