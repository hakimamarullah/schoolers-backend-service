package com.schoolers.repository;

import com.schoolers.models.InformationRead;
import com.schoolers.models.InformationReadId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformationReadRepository extends JpaRepository<InformationRead, InformationReadId> {



    boolean existsByInformationIdAndUserId(Long infoId, Long userId);
}