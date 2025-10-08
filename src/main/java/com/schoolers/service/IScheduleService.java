package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.request.ScheduleRequest;
import com.schoolers.dto.response.ClassroomSchedulesInfo;
import com.schoolers.dto.response.ScheduleResponse;
import com.schoolers.enums.DayOfWeek;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IScheduleService {

    ApiResponse<ScheduleResponse> create(ScheduleRequest requestDto);

    ApiResponse<ScheduleResponse> update(ScheduleRequest requestDto);

    ApiResponse<ScheduleResponse> findById(Long id);

    ApiResponse<PagedResponse<ScheduleResponse>> findAll(Pageable pageable);

    void delete(Long id);

    void softDelete(Long id);

    ApiResponse<ClassroomSchedulesInfo> findByClassroomId(Long classroomId);

    ApiResponse<List<ScheduleResponse>> findByTeacherId(Long teacherId);

    ApiResponse<List<ScheduleResponse>> findByClassroomAndDay(Long classroomId, DayOfWeek dayOfWeek);

    ApiResponse<List<ScheduleResponse>> findByTeacherAndDay(Long teacherId, DayOfWeek dayOfWeek);

    ApiResponse<List<ScheduleResponse>> findByAcademicYear(String academicYear);

    boolean hasScheduleConflict(ScheduleRequest requestDto, Long excludeScheduleId);
}
