package com.schoolers.repository;

import com.schoolers.dto.projection.StudentClassroomInfo;
import com.schoolers.models.Student;
import com.schoolers.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNumber(String studentNumber);
    Optional<Student> findByUser(User user);
    boolean existsByStudentNumber(String studentNumber);

    @Query("SELECT s.classroom.id as id, s.classroom.name as name, s.classroom.grade as grade FROM Student s WHERE s.studentNumber = :studentNumber")
    Optional<StudentClassroomInfo> getStudentClassroomByStudentNumber(String studentNumber);
}
