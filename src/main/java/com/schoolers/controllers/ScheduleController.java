package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.response.ClassroomSchedulesInfo;
import com.schoolers.dto.response.ScheduleResponse;
import com.schoolers.enums.DayOfWeek;
import com.schoolers.service.IScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@LogRequestResponse
@SecurityRequirement(name = "bearerJWT")
public class ScheduleController {

    private final IScheduleService scheduleService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleById(@PathVariable Long id) {
        return scheduleService.findById(id).toResponseEntity();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ScheduleResponse>>> getAllSchedules(@ParameterObject Pageable pageable) {
        return scheduleService.findAll(pageable).toResponseEntity();
    }


    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<ApiResponse<ClassroomSchedulesInfo>> getSchedulesByClassroom(@PathVariable Long classroomId, HttpServletRequest request) {
        return scheduleService.findByClassroomId(classroomId).toResponseEntity();
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByTeacher(@PathVariable Long teacherId) {
        return scheduleService.findByTeacherId(teacherId).toResponseEntity();
    }

    @GetMapping("/classroom/{classroomId}/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByClassroomAndDay(@PathVariable Long classroomId,
                                                                                             @PathVariable DayOfWeek dayOfWeek) {
        return scheduleService.findByClassroomAndDay(classroomId, dayOfWeek).toResponseEntity();
    }

    @GetMapping("/teacher/{teacherId}/day/{dayOfWeek}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByTeacherAndDay(@PathVariable Long teacherId,
                                                                                           @PathVariable DayOfWeek dayOfWeek) {
        return scheduleService.findByTeacherAndDay(teacherId, dayOfWeek).toResponseEntity();
    }

    @GetMapping("/academic-year/{academicYear}")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByAcademicYear(@PathVariable String academicYear) {
        return scheduleService.findByAcademicYear(academicYear).toResponseEntity();
    }

}
