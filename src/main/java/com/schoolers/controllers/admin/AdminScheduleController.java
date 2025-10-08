package com.schoolers.controllers.admin;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.ScheduleRequest;
import com.schoolers.dto.response.ScheduleResponse;
import com.schoolers.service.IScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/schedules")
@RequiredArgsConstructor
@LogRequestResponse
@RolesAllowed({"OFFICE_ADMIN"})
@SecurityRequirement(name = "bearerJWT")
public class AdminScheduleController {

    private final IScheduleService scheduleService;


    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(@Valid @RequestBody ScheduleRequest payload) {
        return scheduleService.create(payload).toResponseEntity();
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(@Valid @RequestBody ScheduleRequest payload) {
        return scheduleService.update(payload).toResponseEntity();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long id) {
        scheduleService.delete(id);
        return ApiResponse.setSuccessWithMessage("Schedule deleted successfully").toResponseEntity();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateSchedule(@PathVariable Long id) {
        scheduleService.softDelete(id);
        return ApiResponse.setSuccessWithMessage("Schedule deactivated successfully").toResponseEntity();
    }

    @PostMapping("/check-conflict")
    public ResponseEntity<ApiResponse<Boolean>> checkScheduleConflict(@Valid @RequestBody ScheduleRequest payload,
                                                                      @RequestParam(required = false) Long excludeScheduleId) {
        boolean hasConflict = scheduleService.hasScheduleConflict(payload, excludeScheduleId);
        return ApiResponse.setResponse(
                hasConflict,
                hasConflict ? "Schedule conflict detected" : "No conflict found",
                HttpStatus.OK.value()
        ).toResponseEntity();
    }
}
