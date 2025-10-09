package com.schoolers.models;

import com.schoolers.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.aot.hint.annotation.RegisterReflection;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "attendance_session_id"}),
        indexes = {
                @Index(name = "idx_student_session", columnList = "student_id, attendance_session_id"),
                @Index(name = "idx_session_status", columnList = "attendance_session_id, status")
        })
@Setter
@Getter
@RegisterReflection
public class StudentAttendance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_session_id", nullable = false)
    private AttendanceSession attendanceSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;
}