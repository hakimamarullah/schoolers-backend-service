package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.request.CreateAssignmentRequest;
import com.schoolers.dto.request.UpdateAssignmentRequest;
import com.schoolers.dto.response.AssignmentResponse;
import com.schoolers.service.IAssignmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@LogRequestResponse
@SecurityRequirement(name = "bearerJWT")
public class AssignmentController {

    private final IAssignmentService assignmentService;


    /**
     * Create a new assignment
     * Only teachers can create assignments
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request,
            Authentication authentication) {
        return assignmentService.createAssignment(request, authentication.getName()).toResponseEntity();
    }

    /**
     * Update an existing assignment
     * Only the assignment owner can update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request,
            Authentication authentication) {
        return assignmentService.updateAssignment(id, request, authentication.getName()).toResponseEntity();
    }

    /**
     * Delete an assignment
     * Only the assignment owner can delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id,
            Authentication authentication) {
        assignmentService.deleteAssignment(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get assignment by ID
     * Accessible by teachers and students
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<AssignmentResponse> getAssignment(@PathVariable Long id) {
        AssignmentResponse response = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all assignments for a classroom
     * Paginated results sorted by due date descending
     */
    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<AssignmentResponse>>> getAssignmentsByClassroom(
            @PathVariable Long classroomId,
            @ParameterObject @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        var response = assignmentService.getAssignmentsByClassroom(classroomId, pageable);
        return response.toResponseEntity();
    }

    /**
     * Get all assignments created by the authenticated teacher
     */
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<PagedResponse<AssignmentResponse>>> getMyAssignments(
            Authentication authentication,
            @ParameterObject @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        var response = assignmentService.getAssignmentsByTeacher(authentication.getName(), pageable);
        return response.toResponseEntity();
    }

    /**
     * Get assignments filtered by classroom and subject
     */
    @GetMapping("/classroom/{classroomId}/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PagedResponse<AssignmentResponse>>> getAssignmentsByClassroomAndSubject(
            @PathVariable Long classroomId,
            @PathVariable Long subjectId,
            @ParameterObject @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        ApiResponse<PagedResponse<AssignmentResponse>> response = assignmentService.getAssignmentsByClassroomAndSubject(
                classroomId, subjectId, pageable);
        return response.toResponseEntity();
    }

    /**
     * Get upcoming assignments for a classroom
     * Returns assignments with future due dates
     */
    @GetMapping("/classroom/{classroomId}/upcoming")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<List<AssignmentResponse>> getUpcomingAssignments(
            @PathVariable Long classroomId) {
        List<AssignmentResponse> response = assignmentService.getUpcomingAssignments(classroomId);
        return ResponseEntity.ok(response);
    }

    /**
     * Add resources to an assignment (files and/or URLs)
     * Supports batch upload of multiple files and URLs in a single request
     *
     * @param id            Assignment ID
     * @param files         List of files to upload (optional)
     * @param resourceNames List of names for URL resources (optional, maps to resourceUrls by index)
     * @param resourceUrls  List of URLs to add as resources (optional)
     * @return Updated assignment with new resources
     * Example usage:
     * - Upload files only: files=[file1.pdf, file2.docx]
     * - Add URLs only: resourceUrls=[url1, url2], resourceNames=[name1, name2]
     * - Mix both: files=[file1.pdf], resourceUrls=[url1], resourceNames=[name1]
     */
    @PostMapping("/{id}/resources")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssignmentResponse> addResources(
            @PathVariable Long id,
            @RequestParam(value = "files", required = false) @Valid @Size(max = 2, message = "Maximum 2 files can be uploaded") List<MultipartFile> files,
            @RequestParam(value = "resourceNames", required = false) List<String> resourceNames,
            @RequestParam(value = "resourceUrls", required = false) List<String> resourceUrls,
            Authentication authentication) {
        AssignmentResponse response = assignmentService.addResources(
                id, files, resourceNames, resourceUrls, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a specific resource from an assignment
     * Only the assignment owner can delete resources
     */
    @DeleteMapping("/{assignmentId}/resources/{resourceId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long assignmentId,
            @PathVariable Long resourceId,
            Authentication authentication) {
        assignmentService.deleteResource(assignmentId, resourceId, authentication.getName());
        return ResponseEntity.noContent().build();
    }


}