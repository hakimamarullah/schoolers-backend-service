package com.schoolers.models;

import com.schoolers.enums.AuthMethod;
import com.schoolers.enums.FailureReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "auth_attempts", indexes = {
        @Index(name = "idx_login_id_created", columnList = "login_id, created_at"),
        @Index(name = "idx_ip_created", columnList = "ip_address, created_at")
})
@Setter
@Getter
public class AuthAttempt extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id")
    private String loginId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthMethod attemptMethod;

    @Column(nullable = false)
    private Boolean successful;

    @Enumerated(EnumType.STRING)
    private FailureReason failureReason;

    @Column(name = "device_id")
    private String deviceId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "biometric_credential_id")
    private Long biometricCredentialId;


}
