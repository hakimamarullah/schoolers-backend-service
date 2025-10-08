package com.schoolers.controllers.admin;


import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.CreateClassroomRequest;
import com.schoolers.dto.request.UpdateClassroomRequest;
import com.schoolers.dto.response.ClassroomInfo;
import com.schoolers.service.IClassroomService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/classrooms")
@LogRequestResponse
@RequiredArgsConstructor
@RolesAllowed({"OFFICE_ADMIN"})
@SecurityRequirement(name = "bearerJWT")
public class AdminClassroomController {

    private final IClassroomService classroomService;

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ClassroomInfo>> addClassroom(@RequestBody CreateClassroomRequest payload) {
        return classroomService.addClassroom(payload).toResponseEntity();
    }

    @PutMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateClassroom(@RequestBody UpdateClassroomRequest payload) {
        classroomService.updateClassroom(payload);
        return ApiResponse.<Void>setSuccess(null).toResponseEntity();
    }

}
