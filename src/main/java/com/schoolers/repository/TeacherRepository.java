package com.schoolers.repository;

import com.schoolers.dto.projection.TeacherInfo;
import com.schoolers.models.Teacher;
import com.schoolers.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmployeeNumber(String employeeNumber);
    Optional<Teacher> findByUser(User user);
    boolean existsByEmployeeNumber(String employeeNumber);

    @Query("SELECT u.fullName as fullName FROM User u JOIN Teacher t ON u.loginId = t.employeeNumber WHERE t.id = :id")
    Optional<TeacherInfo> getTeacherInfo(Long id);

    @Query("SELECT u.fullName as fullName, t.id as id FROM User u JOIN Teacher t ON u.loginId = t.employeeNumber WHERE t.id IN :teacherIds")
    List<TeacherInfo> findTeacherInfoByIdIn(List<Long> teacherIds);
}
