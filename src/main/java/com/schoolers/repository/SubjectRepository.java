package com.schoolers.repository;

import com.schoolers.models.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByCode(String code);

    @Query("SELECT s.name FROM Subject s WHERE s.id = :id")
    Optional<String> getNameById(Long id);
}
