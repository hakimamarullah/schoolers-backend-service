package com.schoolers.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.aot.hint.annotation.RegisterReflection;

@Entity
@Table(name = "attendance_summary", indexes = {
        @Index(name = "idx_student_subject", columnList = "student_id, subject_id"),
        @Index(name = "idx_student_classroom", columnList = "student_id, classroom_id")
})
@Setter
@Getter
@RegisterReflection
public class AttendanceSummary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(nullable = false)
    private Integer totalSessions = 0;

    @Column(nullable = false)
    private Integer attendedSessions = 0;

    @Column(nullable = false)
    private Integer absentSessions = 0;

    @Column(nullable = false)
    private Integer lateSessions = 0;

}
