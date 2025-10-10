package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.ClockInRequest;
import com.schoolers.dto.response.AttendanceResponse;
import com.schoolers.enums.AttendanceStatus;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.AttendanceSession;
import com.schoolers.models.Student;
import com.schoolers.models.StudentAttendance;
import com.schoolers.repository.AttendanceSessionRepository;
import com.schoolers.repository.StudentAttendanceRepository;
import com.schoolers.repository.StudentRepository;
import com.schoolers.service.ILocalizationService;
import com.schoolers.service.IStudentAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
@RegisterReflectionForBinding({
        ClockInRequest.class,
        AttendanceResponse.class
})
public class StudentAttendanceService implements IStudentAttendanceService {

    private final StudentAttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final ILocalizationService localizationService;

    @Override
    @Transactional
    public ApiResponse<AttendanceResponse> clockIn(ClockInRequest request) {
        log.info("Processing clock in for student: {} at session: {}",
                request.getStudentId(), request.getSessionId());

        // Validate session exists and is active
        AttendanceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("attendance.session-not-found")));

        // Validate student exists
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("student.student-not-found")));

        // Check if already clocked in
        if (attendanceRepository.existsByStudentIdAndAttendanceSessionId(
                request.getStudentId(), request.getSessionId())) {
            return ApiResponse.setResponse(null, localizationService.getMessage("attendance.already-clock-in"), 409);
        }

        LocalDateTime clockInTime = LocalDateTime.now();

        // Determine if late based on session start time
        LocalTime sessionStartTime = session.getStartTime();
        LocalTime currentTime = clockInTime.toLocalTime();
        boolean isLate = currentTime.isAfter(sessionStartTime);

        // Create attendance record
        StudentAttendance attendance = new StudentAttendance();
        attendance.setStudent(student);
        attendance.setAttendanceSession(session);
        attendance.setClockInTime(clockInTime);
        attendance.setLatitude(request.getLatitude());
        attendance.setLongitude(request.getLongitude());
        attendance.setStatus(isLate ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);

        StudentAttendance saved = attendanceRepository.save(attendance);

        log.info("Student {} clocked in successfully with status: {}",
                student.getId(), saved.getStatus());

        AttendanceResponse response = buildAttendanceResponse(saved, student, isLate);
        return ApiResponse.setSuccess(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AttendanceResponse> getAttendanceById(Long attendanceId) {
        log.info("Fetching attendance record: {}", attendanceId);

        StudentAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("attendance.record-not-found")));

        Student student = attendance.getStudent();
        boolean isLate = attendance.getStatus() == AttendanceStatus.LATE;

        AttendanceResponse response = buildAttendanceResponse(attendance, student, isLate);
        return ApiResponse.setSuccess(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AttendanceResponse> getStudentSessionAttendance(Long sessionId, String studentNumber) {
        log.info("Fetching attendance for student: {} in session: {}", studentNumber, sessionId);

        StudentAttendance attendance = attendanceRepository
                .findByStudentStudentNumberAndAttendanceSessionId(studentNumber, sessionId)
                .orElse(null);

        if (attendance == null) {
            return ApiResponse.setResponse(null, localizationService.getMessage("attendance.record-not-found"), 404);
        }

        Student student = attendance.getStudent();
        boolean isLate = attendance.getStatus() == AttendanceStatus.LATE;

        AttendanceResponse response = buildAttendanceResponse(attendance, student, isLate);
        return ApiResponse.setSuccess(response);
    }

    private AttendanceResponse buildAttendanceResponse(StudentAttendance attendance,
                                                       Student student,
                                                       boolean isLate) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(student.getId())
                .sessionId(attendance.getAttendanceSession().getId())
                .status(attendance.getStatus())
                .clockInTime(attendance.getClockInTime())
                .latitude(attendance.getLatitude())
                .longitude(attendance.getLongitude())
                .isLate(isLate)
                .build();
    }
}