package com.schoolers.repository;

import com.schoolers.dto.projection.StudentClassroomInfo;
import com.schoolers.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentNumber(String studentNumber);

    @Query("SELECT s.classroom.id as id, s.classroom.name as name, s.classroom.grade as grade FROM Student s WHERE s.studentNumber = :studentNumber")
    Optional<StudentClassroomInfo> getStudentClassroomByStudentNumber(String studentNumber);

    @Modifying
    @Query("UPDATE Student s SET s.classroom.id = :classroomId, s.updatedBy = :studentNumber," +
            " s.updatedDate = CURRENT_TIMESTAMP, s.version = s.version + 1" +
            " WHERE s.studentNumber = :studentNumber")
    int updateStudentById(String studentNumber, Long classroomId);

    long countByClassroomId(Long classroomId);

    Optional<Student> findByStudentNumber(String studentNumber);

    @Query("SELECT s.id FROM Student s WHERE s.classroom.id = :classroomId")
    List<Long> findAllIdByClassroomId(Long classroomId);

    @Query("SELECT DISTINCT s.user.id FROM Student s WHERE s.classroom.id IN :classroomIds")
    Set<Long> getUserIdByClassroomIdIn(Set<Long> classroomIds);

    @Query("SELECT DISTINCT s.user.id FROM Student s WHERE s.classroom.id = :classroomId")
    Set<Long> getUserIdByClassroomId(Long classroomId);
}
