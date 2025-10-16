package com.schoolers.repository;

import com.schoolers.dto.projection.UserToken;
import com.schoolers.enums.OSType;
import com.schoolers.models.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByToken(String token);

    @Query("SELECT dt.token FROM DeviceToken dt WHERE dt.user.loginId = :userId and dt.active = :active")
    List<String> getTokenUserLoginIdAndActive(String userId, Boolean active);

    List<DeviceToken> findByUserLoginIdAndActive(String loginId, Boolean active);

    List<DeviceToken> findByUserLoginId(String userId);

    Optional<DeviceToken> findByUserLoginIdAndTokenAndOsType(String userId, String token, OSType osType);

    void deleteByToken(String token);

    void deleteByUserLoginIdAndToken(String loginId, String token);


    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.active = :status, " +
            "dt.updatedDate = CURRENT_TIMESTAMP, dt.updatedBy = 'SYSTEM', " +
            "dt.version = dt.version + 1 WHERE dt.token IN :tokens")
    int updateByTokenInSetActive(Set<String> tokens, boolean status);

    @Query("SELECT DISTINCT dt.token as token, dt.user.id as ownerId FROM DeviceToken dt WHERE dt.user.id IN :userIds")
    Set<UserToken> getAllTokenByUserIdIn(Set<Long> userIds);

    @Query("""
     SELECT distinct s.id as ownerId, dt.token as token
     FROM Student s
     LEFT JOIN s.user u
     LEFT JOIN DeviceToken dt ON dt.user = u
     WHERE s.id IN :studentIds AND dt.active = true
     """)
    List<UserToken> getUserTokenByStudentIdIn(Set<Long> studentIds);


    @Query("""
    SELECT u.locale as locale, dt.token as token
    FROM Student s
    LEFT JOIN s.user u
    LEFT JOIN DeviceToken dt ON dt.user = u
    WHERE s.classroom.id = :classroomId AND dt.active = true AND dt.token is not null
    """)
    List<UserToken> getAllTokenByClassroomId(Long classroomId);


}
