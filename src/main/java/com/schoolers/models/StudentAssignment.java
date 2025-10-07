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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "assignment_id"}),
        indexes = {
                @Index(name = "idx_student_done", columnList = "student_id, is_done"),
                @Index(name = "idx_assignment_done", columnList = "assignment_id, is_done")
        })
@Setter
@Getter
@RegisterReflection
public class StudentAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false)
    private Boolean isDone = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    private String notes;
}

