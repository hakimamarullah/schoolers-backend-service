package com.schoolers.repository;

import com.schoolers.models.AssignmentResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssignmentResourceRepository extends JpaRepository<AssignmentResource, Long> {
    List<AssignmentResource> findByAssignmentId(Long assignmentId);
    void deleteByAssignmentId(Long assignmentId);

    @Query("SELECT ar.resourcePath FROM AssignmentResource ar WHERE ar.assignment.id = :assignmentId AND ar.resourceType = 'FILE'")
    List<String> getResourcePathByAssignmentId(Long assignmentId);
}