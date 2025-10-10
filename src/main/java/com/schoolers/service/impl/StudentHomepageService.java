package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.projection.TeacherInfo;
import com.schoolers.dto.response.AttendanceInfo;
import com.schoolers.dto.response.AttendanceStats;
import com.schoolers.dto.response.HomepageResponse;
import com.schoolers.dto.response.SessionCard;
import com.schoolers.enums.SessionStatus;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.AttendanceSession;
import com.schoolers.models.Classroom;
import com.schoolers.models.Schedule;
import com.schoolers.models.Student;
import com.schoolers.models.StudentAttendance;
import com.schoolers.repository.AttendanceSessionRepository;
import com.schoolers.repository.StudentAttendanceRepository;
import com.schoolers.repository.StudentRepository;
import com.schoolers.repository.TeacherRepository;
import com.schoolers.service.ILocalizationService;
import com.schoolers.service.IStudentHomePageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@RegisterReflectionForBinding({
        SessionCard.class,
        AttendanceStats.class,
        HomepageResponse.class,
        AttendanceInfo.class,
})
public class StudentHomepageService implements IStudentHomePageService {


    private final StudentRepository studentRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final StudentAttendanceRepository attendanceRepository;
    private final TeacherRepository teacherRepository;
    private final ILocalizationService localizationService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<HomepageResponse> getStudentHomepage(String studentNumber, LocalDate date) {
        log.info("Fetching homepage for student: {} on date: {}", studentNumber, date);

        // Validate student
        Student student = studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("homepage.student-not-found")));

        LocalDate targetDate = Optional.ofNullable(date).orElse(LocalDate.now());
        LocalTime currentTime = LocalTime.now();

        // Get all sessions for the day
        List<AttendanceSession> allSessions = sessionRepository
                .findByClassroomAndDate(student.getClassroom().getId(), targetDate);

        // Categorize sessions
        List<SessionCard> ongoing = new ArrayList<>();
        List<SessionCard> finished = new ArrayList<>();
        List<SessionCard> upcoming = new ArrayList<>();
        List<SessionCard> cancelled = new ArrayList<>();
        int finishedCount = 0;

        List<Long> teacherIds = allSessions.stream().map(it -> it.getTeacher().getId())
                .toList();
        Map<Long, String> teacherName = teacherRepository.findTeacherInfoByIdIn(teacherIds)
                .stream()
                .collect(Collectors.toMap(TeacherInfo::getId, TeacherInfo::getFullName));
        for (AttendanceSession session : allSessions) {
            SessionCard card = buildSessionCard(session, studentNumber, teacherName);

            if (session.getStatus() == SessionStatus.CANCELLED) {
                cancelled.add(card);
            } else if (isSessionFinished(session, currentTime, targetDate)) {
                finishedCount++;
                finished.add(card);
            } else if (isSessionOngoing(session, currentTime, targetDate)) {
                ongoing.add(card);
            } else if (isSessionUpcoming(session, currentTime, targetDate)) {
                upcoming.add(card);
            }
        }

        // Build response
        HomepageResponse response = HomepageResponse.builder()
                .attendanceStats(AttendanceStats.builder()
                        .date(targetDate)
                        .dayName(targetDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                        .currentTime(currentTime)
                        .finishedClasses(finishedCount)
                        .totalClasses(allSessions.size() - cancelled.size())
                        .build())
                .ongoingSessions(ongoing)
                .upcomingSessions(upcoming)
                .cancelledSessions(cancelled)
                .finishedSessions(finished)
                .build();

        return ApiResponse.setSuccess(response);
    }

    private SessionCard buildSessionCard(AttendanceSession session, String studentNumber, Map<Long, String> teacherName) {
        // Get attendance info
        AttendanceInfo attendanceInfo = getAttendanceInfo(session.getId(), studentNumber);

        // Format datetime
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String datetime = String.format("%s, %s-%s",
                session.getSessionDate().getDayOfWeek().getDisplayName(TextStyle.FULL, LocaleContextHolder.getLocale()),
                session.getStartTime().format(timeFormatter),
                session.getEndTime().format(timeFormatter));

        Long scheduleId = Optional.ofNullable(session.getSchedule())
                .map(Schedule::getId)
                .orElse(null);
        String room = Optional.ofNullable(session.getSchedule())
                .map(Schedule::getClassroom)
                .map(Classroom::getName)
                .orElse("TBA");
        return SessionCard.builder()
                .sessionId(session.getId())
                .scheduleId(scheduleId)
                .subjectName(session.getSubject().getName())
                .room(room)
                .teacherName(teacherName.get(session.getTeacher().getId()))
                .datetime(datetime)
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .topic(session.getTopic())
                .attendanceInfo(attendanceInfo)
                .status(session.getStatus())
                .build();
    }

    private AttendanceInfo getAttendanceInfo(Long sessionId, String studentNumber) {
        AttendanceSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return AttendanceInfo.builder()
                    .attendedSessions(0)
                    .totalSessions(0)
                    .hasClocked(false)
                    .build();
        }

        Student student = studentRepository.findByStudentNumber(studentNumber).orElse(null);
        if (student == null) {
            return AttendanceInfo.builder()
                    .attendedSessions(0)
                    .totalSessions(0)
                    .hasClocked(false)
                    .build();
        }

        // Count how many sessions the student attended for this subject
        long attendedCount = attendanceRepository.countAttendedByStudentAndSubject(
                studentNumber,
                session.getSubject().getId()
        );

        // Count total sessions held for this subject in student's classroom
        long totalSessions = attendanceRepository.countTotalSessionsBySubjectAndClassroom(
                session.getSubject().getId(),
                student.getClassroom().getId()
        );

        // Check if student has clocked in for THIS specific session
        Optional<StudentAttendance> studentAttendance = attendanceRepository
                .findByStudentStudentNumberAndAttendanceSessionId(studentNumber, sessionId);

        boolean hasClocked = studentAttendance.isPresent();
        String clockInTime = null;

        if (hasClocked && studentAttendance.get().getClockInTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            clockInTime = studentAttendance.get().getClockInTime().format(formatter);
        }

        return AttendanceInfo.builder()
                .attendedSessions((int) attendedCount)
                .totalSessions((int) totalSessions)
                .hasClocked(hasClocked)
                .clockInTime(clockInTime)
                .build();
    }

    private boolean isSessionOngoing(AttendanceSession session, LocalTime currentTime, LocalDate targetDate) {
        if (SessionStatus.ONGOING.equals(session.getStatus())) {
            return true;
        }
        if (!session.getSessionDate().equals(targetDate)) {
            return false;
        }
        return !currentTime.isBefore(session.getStartTime()) &&
                !currentTime.isAfter(session.getEndTime());
    }

    private boolean isSessionUpcoming(AttendanceSession session, LocalTime currentTime, LocalDate targetDate) {
        if (session.getSessionDate().isAfter(targetDate)) {
            return true;
        }
        if (session.getSessionDate().equals(targetDate)) {
            return currentTime.isBefore(session.getStartTime());
        }
        return false;
    }

    private boolean isSessionFinished(AttendanceSession session, LocalTime currentTime, LocalDate targetDate) {
        if (session.getSessionDate().isBefore(targetDate)) {
            return true;
        }
        if (session.getSessionDate().equals(targetDate)) {
            return currentTime.isAfter(session.getEndTime());
        }
        return false;
    }

}
