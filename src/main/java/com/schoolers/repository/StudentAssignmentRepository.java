package com.schoolers.repository;

import com.schoolers.dto.projection.StudentAssignmentInfo;
import com.schoolers.models.StudentAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StudentAssignmentRepository extends JpaRepository<StudentAssignment, Long> {

    @Query("""
    SELECT sa.id as id, sa.assignment.title as title, sa.assignment.dueDate as dueDate,
    sa.assignment.subject.name as subjectName,
    sa.assignment.id as parentAssignmentId,
    sa.assignment.description as description,
    sa.completedAt as completedAt,
    sa.status as status FROM StudentAssignment sa
    WHERE sa.student.studentNumber = :studentNumber and sa.assignment.classroom.id = sa.student.classroom.id
    """)
    Page<StudentAssignmentInfo> findByStudentStudentNumber(String studentNumber, Pageable pageable);


    @Query("""
    SELECT sa.id as id, sa.assignment.title as title, sa.assignment.dueDate as dueDate,
    sa.assignment.subject.name as subjectName,
    sa.assignment.id as parentAssignmentId,
    sa.assignment.description as description,
    sa.completedAt as completedAt,
    sa.status as status FROM StudentAssignment sa
    WHERE sa.student.studentNumber = :studentNumber
    AND sa.id = :id
    """)
    Optional<StudentAssignmentInfo> findByStudentStudentNumberAndId(String studentNumber, Long id);

    @EntityGraph(attributePaths = {"assignment"})
    Optional<StudentAssignment> findByIdAndStudentStudentNumber(Long id, String studentNumber);
}
