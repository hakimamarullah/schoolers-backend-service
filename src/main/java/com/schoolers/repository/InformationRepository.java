package com.schoolers.repository;

import com.schoolers.dto.projection.InformationDTO;
import com.schoolers.models.Information;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InformationRepository extends JpaRepository<Information, Long> {


    Optional<Information> findFirstById(@Param("id") Long id);

    int countByIdAndUserTargetsUserId(Long id, Long userId);

    int countByIdAndClassroomTargetsClassroomId(Long id, String classroomId);

    int countByIdAndRoleTargetsRole(Long id, String role);

    @Query("""
            SELECT
                i.id as id,
                i.title as title,
                i.body as body,
                i.bannerUri as bannerUri,
                i.createdDate as createdAt,
                i.author.fullName as fullName,
                CASE WHEN EXISTS (
                    SELECT 1 FROM InformationRead ir
                    WHERE ir.information = i AND ir.user.id = :userId
                )
                THEN true ELSE false END as hasRead
            FROM Information i
            WHERE
                EXISTS (
                    SELECT 1 FROM InformationUserTarget ut
                    WHERE ut.information = i AND ut.userId = :userId
                )
                OR EXISTS (
                    SELECT 1 FROM InformationClassroomTarget ct
                    WHERE ct.information = i AND ct.classroomId = :classroomId
                )
                OR EXISTS (
                    SELECT 1 FROM InformationRoleTarget rt
                    WHERE rt.information = i AND rt.role = :role
                )
            ORDER BY i.createdDate DESC
            """)
    Page<InformationDTO> findAllForUser(
            @Param("userId") Long userId,
            @Param("classroomId") String classroomId,
            @Param("role") String role,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(i)
            FROM Information i
            WHERE i IN (
                SELECT ut.information FROM InformationUserTarget ut WHERE ut.userId = :userId
                UNION
                SELECT ct.information FROM InformationClassroomTarget ct WHERE ct.classroomId = :classroomId
                UNION
                SELECT rt.information FROM InformationRoleTarget rt WHERE rt.role = :role
            )
            AND NOT EXISTS (
                SELECT 1 FROM InformationRead ir
                WHERE ir.information.id = i.id AND ir.user.id = :userId
            )
            """)
    long countUnreadForUser(
            @Param("userId") Long userId,
            @Param("classroomId") String classroomId,
            @Param("role") String role
    );


}