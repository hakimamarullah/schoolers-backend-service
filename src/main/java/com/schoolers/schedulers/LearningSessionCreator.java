package com.schoolers.schedulers;

import com.schoolers.dto.projection.SimpleScheduleInfo;
import com.schoolers.enums.DayOfWeek;
import com.schoolers.enums.SessionStatus;
import com.schoolers.models.AttendanceSession;
import com.schoolers.models.Classroom;
import com.schoolers.models.Schedule;
import com.schoolers.models.Subject;
import com.schoolers.models.Teacher;
import com.schoolers.repository.AttendanceSessionRepository;
import com.schoolers.repository.ScheduleRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class LearningSessionCreator {

    private final ScheduleRepository scheduleRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final EntityManager entityManager;

    @Scheduled(cron = "${scheduler.learning-session-cron:0 0 3 * * *}")
    @Transactional
    @Modifying
    @Retryable(backoff = @Backoff(delay = 4000))
    public void setupDailySession() {
        LocalDate today = LocalDate.now();
        String currentDay = today.getDayOfWeek().name();
        log.info("[START] Creating attendance sessions for {}", currentDay);

        int page = 0;
        int size = 50;
        long processedCounter = 0;
        Page<SimpleScheduleInfo> schedulePage;

        do {
            Pageable pageable = PageRequest.of(page, size);
            schedulePage = scheduleRepository.getAllByDayOfWeekAndActiveIsTrue(DayOfWeek.valueOf(currentDay), pageable);

            // Collect schedule IDs
            List<Long> scheduleIds = schedulePage.getContent().stream()
                    .map(SimpleScheduleInfo::getId)
                    .toList();

            // Find already existing sessions for today
            Set<Long> existingScheduleIds = new HashSet<>(
                    attendanceSessionRepository.findAllScheduleIdsBySessionDate(today, scheduleIds)
            );

            // Create only for missing schedules
            List<SimpleScheduleInfo> newSchedules = schedulePage.getContent().stream()
                    .filter(it -> !existingScheduleIds.contains(it.getId()))
                    .toList();

            if (!newSchedules.isEmpty()) {
                List<AttendanceSession> sessions = createSessions(newSchedules, today);
                attendanceSessionRepository.saveAllAndFlush(sessions);
                processedCounter += sessions.size();
            }

            page++;
        } while (schedulePage.hasNext());

        log.info("[END] Attendance sessions created for {}: {}", currentDay, processedCounter);
    }

    private List<AttendanceSession> createSessions(List<SimpleScheduleInfo> schedules, LocalDate today) {
        return schedules.stream()
                .map(it -> {
                    var classroom = entityManager.getReference(Classroom.class, it.getClassroomId());
                    var subject = entityManager.getReference(Subject.class, it.getSubjectId());
                    var teacher = entityManager.getReference(Teacher.class, it.getTeacherId());
                    var schedule = entityManager.getReference(Schedule.class, it.getId());

                    return AttendanceSession.builder()
                            .schedule(schedule)
                            .classroom(classroom)
                            .subject(subject)
                            .teacher(teacher)
                            .startTime(it.getStartTime())
                            .endTime(it.getEndTime())
                            .sessionDate(today)
                            .status(SessionStatus.SCHEDULED)
                            .build();
                })
                .toList();
    }
}
