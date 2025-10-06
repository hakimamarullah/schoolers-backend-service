package com.schoolers.models;

import com.schoolers.enums.BiometricType;
import com.schoolers.enums.DeviceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_credentials", indexes = {
        @Index(name = "idx_user_device", columnList = "user_id, device_id"),
        @Index(name = "idx_public_key_hash", columnList = "public_key_hash")
})
@Setter
@Getter
public class BiometricCredential extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(nullable = false, unique = true)
    private String publicKeyHash;

    @Column(nullable = false)
    private String algorithm;

    @Column(nullable = false)
    private Integer keySize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BiometricType biometricType;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime registeredAt;

    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private Integer failedAttempts = 0;

    private LocalDateTime lockedUntil;


}
