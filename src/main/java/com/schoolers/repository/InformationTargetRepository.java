package com.schoolers.repository;

import com.schoolers.models.InformationTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InformationTargetRepository extends JpaRepository<InformationTarget, Long> {

    @EntityGraph(attributePaths = {"information"})
    Page<InformationTarget> findAllByUserIdOrClassroomIdOrRole(String userId, String classroomId, String role, Pageable pageable);
}
