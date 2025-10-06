package com.schoolers.repository;

import com.schoolers.models.Teacher;
import com.schoolers.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmployeeNumber(String employeeNumber);
    Optional<Teacher> findByUser(User user);
    boolean existsByEmployeeNumber(String employeeNumber);
}
