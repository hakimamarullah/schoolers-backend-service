package com.schoolers.utils;

import com.schoolers.dto.response.ScheduleResponse;
import com.schoolers.models.Schedule;
import org.springframework.stereotype.Component;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
public class ScheduleMapper {

    public ScheduleResponse toDto(Schedule schedule, Locale locale) {
        if (schedule == null) {
            return null;
        }

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .classroomId(schedule.getClassroom().getId())
                .classroomName(schedule.getClassroom().getName())
                .subjectId(schedule.getSubject().getId())
                .subjectName(schedule.getSubject().getName())
                .teacherId(schedule.getTeacher().getId())
                .dayOfWeek(schedule.getDayOfWeek().name())
                .displayDay(schedule.getDayOfWeek().getDisplayName(TextStyle.FULL, locale))
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .academicYear(schedule.getAcademicYear())
                .active(schedule.getActive())
                .createdAt(schedule.getCreatedDate())
                .updatedAt(schedule.getUpdatedDate())
                .build();
    }

    public List<ScheduleResponse> toDtoList(List<Schedule> schedules, Locale locale) {
        if (schedules == null) {
            return List.of();
        }

        return schedules.parallelStream()
                .map(it -> toDto(it, locale))
                .toList();
    }
}
