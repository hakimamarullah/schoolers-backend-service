package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.response.StudentAssignmentResponse;
import com.schoolers.enums.SubmissionStatus;
import org.springframework.data.domain.Pageable;

public interface IStudentAssignmentService {

    ApiResponse<PagedResponse<StudentAssignmentResponse>> getStudentAssigmentByLoginId(String loginId, Pageable pageable);

    ApiResponse<StudentAssignmentResponse> getStudentAssigmentById(Long id, String studentNumber);

    ApiResponse<Void> updateAssignmentStatus(Long assignmentId, String studentNumber, SubmissionStatus submissionStatus);
}
