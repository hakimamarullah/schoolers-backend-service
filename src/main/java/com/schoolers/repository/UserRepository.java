package com.schoolers.repository;

import com.schoolers.dto.projection.UserLocale;
import com.schoolers.enums.UserRole;
import com.schoolers.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByEmail(String email);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE u.loginId = :loginId")
    Optional<Long> getUserIdByLoginId(String loginId);

    @Query("SELECT DISTINCT u.id FROM User u WHERE u.role IN :roles")
    Set<Long> getUserIdByRoleIn(Set<UserRole> roles);

    @Query("SELECT u.locale FROM User u WHERE u.id = :userId")
    Optional<String> getUserLocaleById(Long userId);

    @Query("SELECT u.locale FROM User u WHERE u.loginId = :loginId")
    Optional<String> getUserLocaleByLoginId(String loginId);

    @Query("SELECT s.id as userId, u.locale as locale FROM Student s LEFT JOIN s.user u WHERE s.id IN :studentIds")
    List<UserLocale> getAllLocaleByStudentIdIn(Set<Long> studentIds);

    @Query("SELECT u.id as userId, u.locale as locale FROM User u WHERE u.id IN :userIds")
    List<UserLocale> getAllLocaleByUserIdIn(Set<Long> userIds);

    @Modifying
    @Query("UPDATE User u SET u.locale = :locale WHERE u.loginId = :loginId")
    int updateLocaleByLoginId(String loginId, String locale);

}

