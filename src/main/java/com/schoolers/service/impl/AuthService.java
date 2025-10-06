package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.event.AuthAttemptEvent;
import com.schoolers.dto.projection.StudentClassroomInfo;
import com.schoolers.dto.request.BiometricAuthCompleteRequest;
import com.schoolers.dto.request.BiometricAuthInitRequest;
import com.schoolers.dto.request.BiometricRegistrationRequest;
import com.schoolers.dto.request.LoginRequest;
import com.schoolers.dto.response.AuthResponse;
import com.schoolers.dto.response.BiometricChallengeResponse;
import com.schoolers.dto.response.UserInfo;
import com.schoolers.enums.AuthMethod;
import com.schoolers.enums.ChallengeStatus;
import com.schoolers.enums.FailureReason;
import com.schoolers.enums.UserRole;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.exceptions.DuplicateDataException;
import com.schoolers.exceptions.SignatureException;
import com.schoolers.exceptions.TooManyAttempt;
import com.schoolers.models.AuthSession;
import com.schoolers.models.BiometricChallenge;
import com.schoolers.models.BiometricCredential;
import com.schoolers.models.User;
import com.schoolers.repository.AuthAttemptRepository;
import com.schoolers.repository.AuthSessionRepository;
import com.schoolers.repository.BiometricChallengeRepository;
import com.schoolers.repository.BiometricCredentialRepository;
import com.schoolers.repository.StudentRepository;
import com.schoolers.repository.UserRepository;
import com.schoolers.service.IAuthService;
import com.schoolers.utils.SignatureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {

    public static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;
    private final BiometricCredentialRepository biometricCredentialRepository;
    private final BiometricChallengeRepository biometricChallengeRepository;
    private final StudentRepository studentRepository;
    private final AuthSessionRepository authSessionRepository;
    private final AuthAttemptRepository authAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SignatureUtils signatureUtils;
    private final ApplicationEventPublisher eventPublisher;


    @Value("${security.max-failed-attempts:5}")
    private Integer maxFailedAttempts;

    @Value("${security.lock-duration-minutes:30}")
    private Integer lockDurationMinutes;

    @Value("${security.challenge-expiration-minutes:5}")
    private Integer challengeExpirationMinutes;

    @Value("${school.name:-Unknown}")
    private String schoolName;
    @Transactional
    @Override
    public ApiResponse<AuthResponse> login(LoginRequest payload) {
        String ipAddress = payload.getClientIp();

        // Check rate limiting
        checkRateLimit(payload.getLoginId(), ipAddress);

        // Find user
        User user = userRepository.findByLoginId(payload.getLoginId())
                .orElseThrow(() -> {
                    logFailedAttempt(payload.getLoginId(), null, AuthMethod.PASSWORD,
                            FailureReason.USER_NOT_FOUND, ipAddress, payload.getUserAgent());
                    return new BadCredentialsException("Invalid credentials");
                });

        // Check if user is active
        if (Boolean.FALSE.equals(user.getActive())) {
            logFailedAttempt(payload.getLoginId(), user.getId(), AuthMethod.PASSWORD,
                    FailureReason.USER_INACTIVE, ipAddress, payload.getUserAgent());
            throw new BadCredentialsException("User account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(payload.getPassword(), user.getPassword())) {
            logFailedAttempt(payload.getLoginId(), user.getId(), AuthMethod.PASSWORD,
                    FailureReason.INVALID_CREDENTIALS, ipAddress, payload.getUserAgent());
            throw new BadCredentialsException("Invalid credentials");
        }

        // Successful authentication
        logSuccessfulAttempt(payload.getLoginId(), user.getId(), AuthMethod.PASSWORD,
                ipAddress, payload.getUserAgent());

        log.info("User {} logged in successfully via PASSWORD", payload.getLoginId());

        var body = createAuthResponse(user, AuthMethod.PASSWORD, null,
                payload.getDeviceId(), payload.getDeviceName(), payload.getClientIp(), payload.getUserAgent());
        return ApiResponse.setSuccess(body);
    }

    @Transactional
    @Override
    public ApiResponse<BiometricChallengeResponse> initiateBiometricAuth(BiometricAuthInitRequest request) {
        String ipAddress = request.getClientIp();

        // Find user
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND));

        // Check if user is active
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BadCredentialsException("User account is inactive");
        }

        // Check if biometric is enabled
        if (Boolean.FALSE.equals(user.getBiometricEnabled())) {
            throw new BadCredentialsException("Biometric authentication not enabled for this user");
        }

        // Find biometric credential by public key hash
        BiometricCredential credential = biometricCredentialRepository
                .findByPublicKeyHash(request.getPublicKeyHash())
                .orElseThrow(() -> new BadCredentialsException("Device not registered"));

        // Verify credential belongs to user
        if (!credential.getUser().getId().equals(user.getId())) {
            throw new BadCredentialsException("Credential does not belong to user");
        }

        // Check if credential is active
        if (Boolean.FALSE.equals(credential.getActive())) {
            throw new BadCredentialsException("Credential is inactive");
        }

        // Check if device is locked
        if (credential.getLockedUntil() != null &&
                credential.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new TooManyAttempt("Device is temporarily locked due to failed attempts");
        }

        // Generate random challenge token
        String challengeToken = signatureUtils.generateSecureToken();

        // Create challenge record
        BiometricChallenge challenge = new BiometricChallenge();
        challenge.setUser(user);
        challenge.setBiometricCredential(credential);
        challenge.setChallengeToken(challengeToken);
        challenge.setStatus(ChallengeStatus.PENDING);
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(challengeExpirationMinutes));
        challenge.setIpAddress(ipAddress);

        biometricChallengeRepository.save(challenge);

        log.info("Biometric challenge generated for user: {}, credential: {}",
                user.getLoginId(), credential.getId());

        return ApiResponse.setSuccess(
                new BiometricChallengeResponse(
                        challengeToken,
                        credential.getId(),
                        challenge.getExpiresAt()
                )
        );
    }

    @Transactional
    @Override
    public ApiResponse<AuthResponse> completeBiometricAuth(BiometricAuthCompleteRequest request) {
        String ipAddress = request.getClientIp();

        // Find challenge
        BiometricChallenge challenge = biometricChallengeRepository
                .findByChallengeToken(request.getChallengeToken())
                .orElseThrow(() -> new RuntimeException("Invalid challenge token"));

        // Check if challenge is still pending
        if (challenge.getStatus() != ChallengeStatus.PENDING) {
            throw new BadCredentialsException("Challenge already processed");
        }

        // Check if challenge has expired
        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            challenge.setStatus(ChallengeStatus.EXPIRED);
            biometricChallengeRepository.save(challenge);
            throw new BadCredentialsException("Challenge expired");
        }

        BiometricCredential credential = challenge.getBiometricCredential();
        User user = challenge.getUser();

        // Verify signature using public key
        boolean isValid = signatureUtils.verifySignature(
                challenge.getChallengeToken(),
                request.getSignedChallenge(),
                credential.getPublicKey(),
                credential.getAlgorithm()
        );

        if (!isValid) {
            // Mark challenge as failed
            challenge.setStatus(ChallengeStatus.FAILED);
            challenge.setRespondedAt(LocalDateTime.now());
            biometricChallengeRepository.save(challenge);

            // Increment failed attempts
            credential.setFailedAttempts(credential.getFailedAttempts() + 1);
            if (credential.getFailedAttempts() >= maxFailedAttempts) {
                credential.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
                log.warn("Biometric credential {} locked due to {} failed attempts",
                        credential.getId(), maxFailedAttempts);
            }
            biometricCredentialRepository.save(credential);

            // Log failed attempt
            logFailedAttempt(user.getLoginId(), user.getId(), AuthMethod.BIOMETRIC,
                    FailureReason.INVALID_SIGNATURE, ipAddress, request.getUserAgent());

            throw new SignatureException("Invalid signature");
        }

        // Successful authentication
        challenge.setStatus(ChallengeStatus.VERIFIED);
        challenge.setVerifiedAt(LocalDateTime.now());
        challenge.setSignedResponse(request.getSignedChallenge());
        challenge.setRespondedAt(LocalDateTime.now());
        biometricChallengeRepository.save(challenge);

        // Reset failed attempts
        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);
        credential.setLastUsedAt(LocalDateTime.now());
        biometricCredentialRepository.save(credential);

        // Log successful attempt
        logSuccessfulAttempt(user.getLoginId(), user.getId(), AuthMethod.BIOMETRIC,
                ipAddress, request.getUserAgent());

        log.info("User {} logged in successfully via BIOMETRIC", user.getLoginId());

        return ApiResponse.setSuccess(createAuthResponse(user, AuthMethod.BIOMETRIC, credential,
                request.getDeviceId(), null, request.getClientIp(),request.getUserAgent()));
    }

    @Transactional
    @Override
    public ApiResponse<Long> registerBiometricCredential(BiometricRegistrationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        // Check if device already registered for this user
        biometricCredentialRepository.findByUserAndDeviceIdAndActiveTrue(user, request.getDeviceId())
                .ifPresent(existing -> {
                    throw new DuplicateDataException("Device already registered");
                });

        // Validate public key format
        signatureUtils.validatePublicKey(request.getPublicKey(), request.getAlgorithm());

        // Generate hash of public key
        String publicKeyHash = signatureUtils.generateHash(request.getPublicKey());

        // Create credential
        BiometricCredential credential = new BiometricCredential();
        credential.setUser(user);
        credential.setDeviceId(request.getDeviceId());
        credential.setDeviceName(request.getDeviceName());
        credential.setDeviceType(request.getDeviceType());
        credential.setBiometricType(request.getBiometricType());
        credential.setPublicKey(request.getPublicKey());
        credential.setPublicKeyHash(publicKeyHash);
        credential.setAlgorithm(request.getAlgorithm());
        credential.setKeySize(request.getKeySize());
        credential.setActive(true);

        biometricCredentialRepository.save(credential);

        // Enable biometric for user if not already enabled
        if (Boolean.FALSE.equals(user.getBiometricEnabled())) {
            user.setBiometricEnabled(true);
            userRepository.save(user);
        }

        log.info("Biometric credential registered for user: {}, device: {}",
                user.getLoginId(), request.getDeviceId());

        return ApiResponse.setResponse(credential.getId(), "Biometric credential registered successfully", 200);
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<BiometricCredential>> getUserBiometricCredentials(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return ApiResponse.setSuccess(biometricCredentialRepository.findByUserAndActiveTrue(user));
    }

    @Transactional
    @Modifying
    @Override
    public ApiResponse<Void> revokeBiometricCredential(Long userId, Long biometricCredentialId) {
        BiometricCredential credential = biometricCredentialRepository.findById(biometricCredentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));

        if (!credential.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Unauthorized");
        }

        credential.setActive(false);
        biometricCredentialRepository.save(credential);

        // Check if user has any other active credentials
        List<BiometricCredential> activeCredentials = biometricCredentialRepository
                .findByUserAndActiveTrue(credential.getUser());

        if (activeCredentials.isEmpty()) {
            User user = credential.getUser();
            user.setBiometricEnabled(false);
            userRepository.save(user);
            log.info("Biometric disabled for user: {} (no active credentials)", user.getLoginId());
        }

        log.info("Biometric credential {} revoked for user: {}",
                biometricCredentialId, credential.getUser().getLoginId());

        return ApiResponse.setSuccess(null);
    }

    @Transactional
    @Modifying
    @Override
    public ApiResponse<Void> logout(Long userId, String sessionId) {
        AuthSession session = authSessionRepository.findBySessionIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new DataNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Unauthorized");
        }

        session.setActive(false);
        session.setRevokedAt(LocalDateTime.now());
        session.setRevokedReason("logout");
        authSessionRepository.save(session);

        log.info("User {} logged out, session: {}", userId, sessionId);

        return ApiResponse.setResponse(null, "User logout successfully", 200);
    }


    private AuthResponse createAuthResponse(User user, AuthMethod authMethod,
                                            BiometricCredential credential, String deviceId,
                                            String deviceName, String clientIp, String userAgent) {
        // Generate JWT token
        String token = jwtUtil.generateToken(String.valueOf(user.getId()), user.getLoginId(), user.getRole().name());
        String tokenHash = signatureUtils.generateHash(token);

        // Create session
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setSessionId(UUID.randomUUID().toString());
        session.setAccessTokenHash(tokenHash);
        session.setAuthMethod(authMethod);
        session.setBiometricCredential(credential);
        session.setDeviceId(deviceId);
        session.setDeviceName(deviceName);
        session.setIpAddress(clientIp);
        session.setUserAgent(userAgent);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getExpirationMillis() / 1000));
        session.setActive(true);

        authSessionRepository.save(session);

        // Update user last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .fullName(user.getFullName())
                .profilePictUri(user.getProfilePictUri())
                .email(user.getEmail())
                .role(user.getRole())
                .biometricEnabled(user.getBiometricEnabled())
                .schoolName(schoolName)
                .gender(user.getGender())
                .build();

        if (user.getRole().equals(UserRole.STUDENT)) {
            Optional<StudentClassroomInfo> classroomInfo = studentRepository.getStudentClassroomByStudentNumber(user.getLoginId());
            classroomInfo.ifPresent(it -> {
                userInfo.setClassName(it.getName());
                userInfo.setClassroomId(it.getId());
                userInfo.setGrade(it.getGrade());
            });
        }


        return new AuthResponse(
                token,
                "Bearer",
                jwtUtil.getExpirationMillis() / 1000,
                session.getSessionId(),
                userInfo
        );
    }

    private void checkRateLimit(String loginId, String ipAddress) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(lockDurationMinutes);

        // Check failed attempts by login ID
        long failedAttempts = authAttemptRepository
                .countByLoginIdAndSuccessfulFalseAndCreatedDateAfter(loginId, since);

        if (failedAttempts >= maxFailedAttempts) {
            throw new TooManyAttempt("Account temporarily locked due to too many failed attempts");
        }

        // Check failed attempts by IP
        long ipAttempts = authAttemptRepository
                .countByIpAddressAndSuccessfulFalseAndCreatedDateAfter(ipAddress, since);

        if (ipAttempts >= maxFailedAttempts * 2) {
            throw new TooManyAttempt("Too many failed attempts from this IP address");
        }
    }

    private void logSuccessfulAttempt(String loginId, Long userId, AuthMethod method,
                                      String ipAddress, String userAgent) {
      logAuthAttempt(loginId, userId, method, null, ipAddress, userAgent, true);
    }



    private void logFailedAttempt(String loginId, Long userId, AuthMethod method,
                                  FailureReason reason, String ipAddress,
                                  String userAgent) {

        logAuthAttempt(loginId, userId, method, reason, ipAddress, userAgent, false);


    }

    private void logAuthAttempt(String loginId, Long userId, AuthMethod method,
                                FailureReason reason, String ipAddress,
                                String userAgent, boolean successful) {
        eventPublisher.publishEvent(AuthAttemptEvent.builder()
                .loginId(loginId)
                .successful(successful)
                .userId(userId)
                .method(method)
                .failureReason(reason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build());

    }
}
