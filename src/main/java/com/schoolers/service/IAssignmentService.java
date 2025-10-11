package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.request.CreateAssignmentRequest;
import com.schoolers.dto.request.UpdateAssignmentRequest;
import com.schoolers.dto.response.AssignmentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IAssignmentService {
    ApiResponse<AssignmentResponse> createAssignment(CreateAssignmentRequest request, String teacherId);
    ApiResponse<AssignmentResponse> updateAssignment(Long id, UpdateAssignmentRequest request, String teacherId);
    void deleteAssignment(Long id, String teacherId);
    AssignmentResponse getAssignmentById(Long id);
    ApiResponse<PagedResponse<AssignmentResponse>> getAssignmentsByClassroom(Long classroomId, Pageable pageable);
    ApiResponse<PagedResponse<AssignmentResponse>> getAssignmentsByTeacher(String teacherId, Pageable pageable);
    ApiResponse<PagedResponse<AssignmentResponse>> getAssignmentsByClassroomAndSubject(Long classroomId, Long subjectId, Pageable pageable);
    List<AssignmentResponse> getUpcomingAssignments(Long classroomId);
    AssignmentResponse addResources(Long assignmentId, List<MultipartFile> files, List<String> resourceNames, List<String> resourceUrls, String teacherId);
    void deleteResource(Long assignmentId, Long resourceId, String teacherId);
}
