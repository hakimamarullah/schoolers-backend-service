package com.schoolers.repository;

import com.schoolers.models.InformationRead;
import com.schoolers.models.InformationReadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InformationReadRepository extends JpaRepository<InformationRead, InformationReadId> {

    @Query("SELECT ir FROM InformationRead ir WHERE ir.id.informationId = :informationId AND ir.id.userId = :userId")
    Optional<InformationRead> findByInformationIdAndUserId(
            @Param("informationId") Long informationId,
            @Param("userId") String userId
    );

    boolean existsById(InformationReadId id);
}