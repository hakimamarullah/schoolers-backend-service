package com.schoolers.repository;

import com.schoolers.models.AuthAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AuthAttemptRepository extends JpaRepository<AuthAttempt, Long> {


    long countByLoginIdAndSuccessfulFalseAndCreatedDateAfter(String loginId, LocalDateTime since);

    long countByIpAddressAndSuccessfulFalseAndCreatedDateAfter(String ipAddress, LocalDateTime since);

}
