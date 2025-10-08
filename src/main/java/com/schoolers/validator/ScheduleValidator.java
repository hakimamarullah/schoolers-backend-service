package com.schoolers.validator;

import com.schoolers.dto.request.ScheduleRequest;
import com.schoolers.exceptions.InvalidScheduleException;
import com.schoolers.models.Schedule;
import com.schoolers.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleValidator {

    private final ScheduleRepository scheduleRepository;

    public void validateScheduleRequest(ScheduleRequest requestDto) {
        validateTimeRange(requestDto.getStartTime(), requestDto.getEndTime());
        validateRequiredFields(requestDto);
    }

    public boolean hasTimeConflict(ScheduleRequest requestDto, Long excludeScheduleId) {
        return hasClassroomConflict(requestDto, excludeScheduleId)
                || hasTeacherConflict(requestDto, excludeScheduleId);
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidScheduleException("Start time and end time are required");
        }

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new InvalidScheduleException("Start time must be before end time");
        }

        if (startTime.isBefore(LocalTime.of(6, 0)) || endTime.isAfter(LocalTime.of(22, 0))) {
            throw new InvalidScheduleException("Schedule time must be between 06:00 and 22:00");
        }
    }

    private void validateRequiredFields(ScheduleRequest requestDto) {
        if (requestDto.getClassroomId() == null) {
            throw new InvalidScheduleException("Classroom is required");
        }

        if (requestDto.getSubjectId() == null) {
            throw new InvalidScheduleException("Subject is required");
        }

        if (requestDto.getTeacherId() == null) {
            throw new InvalidScheduleException("Teacher is required");
        }

        if (requestDto.getDayOfWeek() == null) {
            throw new InvalidScheduleException("Day of week is required");
        }
    }

    private boolean hasClassroomConflict(ScheduleRequest requestDto, Long excludeScheduleId) {
        List<Schedule> conflicts = scheduleRepository.findClassroomConflicts(
                requestDto.getClassroomId(),
                requestDto.getDayOfWeek(),
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                excludeScheduleId
        );

        if (!conflicts.isEmpty()) {
            log.warn("Classroom conflict detected for classroom: {} on {}",
                    requestDto.getClassroomId(), requestDto.getDayOfWeek());
            return true;
        }

        return false;
    }

    private boolean hasTeacherConflict(ScheduleRequest requestDto, Long excludeScheduleId) {
        List<Schedule> conflicts = scheduleRepository.findTeacherConflicts(
                requestDto.getTeacherId(),
                requestDto.getDayOfWeek(),
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                excludeScheduleId
        );

        if (!conflicts.isEmpty()) {
            log.warn("Teacher conflict detected for teacher: {} on {}",
                    requestDto.getTeacherId(), requestDto.getDayOfWeek());
            return true;
        }

        return false;
    }
}
