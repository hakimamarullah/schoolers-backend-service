package com.schoolers.repository;

import com.schoolers.models.StudentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, Long> {

    /**
     * Check if student already has attendance record for a session
     */
    boolean existsByStudentIdAndAttendanceSessionId(Long studentId, Long sessionId);

    /**
     * Find attendance record by student and session
     */
    Optional<StudentAttendance> findByStudentStudentNumberAndAttendanceSessionId(String studentNumber, Long sessionId);


    /**
     * Count how many sessions the student attended for a specific subject
     */
    @Query("SELECT COUNT(sa) FROM StudentAttendance sa " +
            "JOIN sa.attendanceSession s " +
            "WHERE sa.student.studentNumber = :studentNumber " +
            "AND s.subject.id = :subjectId " +
            "AND sa.status IN ('PRESENT', 'LATE')")
    long countAttendedByStudentAndSubject(@Param("studentNumber") String studentNumber, @Param("subjectId") Long subjectId);

    /**
     * Count total sessions held for a specific subject and student's classroom
     */
    @Query("SELECT COUNT(s) FROM AttendanceSession s " +
            "WHERE s.subject.id = :subjectId " +
            "AND s.classroom.id = :classroomId " +
            "AND s.status != 'CANCELLED'")
    long countTotalSessionsBySubjectAndClassroom(
            @Param("subjectId") Long subjectId,
            @Param("classroomId") Long classroomId);
}