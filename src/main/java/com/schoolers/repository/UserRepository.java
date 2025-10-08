package com.schoolers.repository;

import com.schoolers.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByEmail(String email);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE u.loginId = :loginId")
    Optional<Long> getUserIdByLoginId(String loginId);
}

