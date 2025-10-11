package com.schoolers.repository;

import com.schoolers.models.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("SELECT a FROM Assignment a " +
            "LEFT JOIN FETCH a.classroom " +
            "LEFT JOIN FETCH a.subject " +
            "LEFT JOIN FETCH a.teacher " +
            "WHERE a.id = :id")
    Optional<Assignment> findByIdWithDetails(@Param("id") Long id);

    Optional<Assignment> findByIdAndTeacherEmployeeNumber(Long id, String employeeNumber);

    Page<Assignment> findByClassroomId(Long classroomId, Pageable pageable);

    Page<Assignment> findByTeacherEmployeeNumber(String teacherId, Pageable pageable);

    Page<Assignment> findByClassroomIdAndSubjectId(Long classroomId, Long subjectId, Pageable pageable);

    List<Assignment> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByIdAndTeacherEmployeeNumber(Long id, String employeeNumber);

    @Query("SELECT a FROM Assignment a WHERE a.classroom.id = :classroomId AND a.dueDate >= :now ORDER BY a.dueDate ASC")
    List<Assignment> findUpcomingByClassroom(@Param("classroomId") Long classroomId, @Param("now") LocalDateTime now);
}
