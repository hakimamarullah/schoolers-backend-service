package com.schoolers.utils;

import com.schoolers.dto.response.ScheduleResponse;
import com.schoolers.models.Schedule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduleMapper {

    public ScheduleResponse toDto(Schedule schedule) {
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
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .academicYear(schedule.getAcademicYear())
                .active(schedule.getActive())
                .createdAt(schedule.getCreatedDate())
                .updatedAt(schedule.getUpdatedDate())
                .build();
    }

    public List<ScheduleResponse> toDtoList(List<Schedule> schedules) {
        if (schedules == null) {
            return List.of();
        }

        return schedules.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
