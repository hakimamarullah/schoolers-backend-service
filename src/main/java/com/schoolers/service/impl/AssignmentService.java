package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.event.NewAssignmentEvent;
import com.schoolers.dto.request.CreateAssignmentRequest;
import com.schoolers.dto.request.UpdateAssignmentRequest;
import com.schoolers.dto.response.AssignmentResourceResponse;
import com.schoolers.dto.response.AssignmentResponse;
import com.schoolers.enums.ResourceType;
import com.schoolers.exceptions.BadRequestException;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.Assignment;
import com.schoolers.models.AssignmentResource;
import com.schoolers.models.Classroom;
import com.schoolers.models.Subject;
import com.schoolers.models.Teacher;
import com.schoolers.repository.AssignmentRepository;
import com.schoolers.repository.AssignmentResourceRepository;
import com.schoolers.repository.ScheduleRepository;
import com.schoolers.repository.TeacherRepository;
import com.schoolers.service.IAssignmentService;
import com.schoolers.service.IFileStorageService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@RegisterReflectionForBinding({
        AssignmentResponse.class,
        CreateAssignmentRequest.class,
        UpdateAssignmentRequest.class,
        AssignmentResource.class,
        NewAssignmentEvent.class
})
public class AssignmentService implements IAssignmentService {

    public static final String ASSIGNMENT_NOT_FOUND = "Assignment not found";
    private final AssignmentRepository assignmentRepository;
    private final AssignmentResourceRepository resourceRepository;
    private final TeacherRepository teacherRepository;
    private final IFileStorageService fileStorageService;
    private final ScheduleRepository scheduleRepository;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public ApiResponse<AssignmentResponse> createAssignment(CreateAssignmentRequest request, String teacherId) {
        if (!scheduleRepository.existsByClassroomIdAndSubjectIdAndTeacherEmployeeNumber(request.getClassroomId(), request.getSubjectId(), teacherId)) {
            throw new BadRequestException("No schedule found for this classroom, subject and teacher");
        }

        Assignment assignment = new Assignment();
        assignment.setClassroom(entityManager.getReference(Classroom.class, request.getClassroomId()));
        assignment.setSubject(entityManager.getReference(Subject.class, request.getSubjectId()));
        assignment.setTeacher(entityManager.getReference(Teacher.class, teacherRepository.getIdByEmployeeNumber(teacherId)));
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());

        assignment = assignmentRepository.save(assignment);
        log.info("Assignment created with ID: {}", assignment.getId());

        eventPublisher.publishEvent(NewAssignmentEvent.builder()
                .assignmentId(assignment.getId())
                .classroomId(request.getClassroomId()).build());

        return ApiResponse.setResponse(mapToResponse(assignment), 201);
    }

    @Transactional
    @Override
    public ApiResponse<AssignmentResponse> updateAssignment(Long id, UpdateAssignmentRequest request, String teacherId) {
        Assignment assignment = assignmentRepository.findByIdAndTeacherEmployeeNumber(id, teacherId)
                .orElseThrow(() -> new DataNotFoundException(ASSIGNMENT_NOT_FOUND));

        if (request.getTitle() != null) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getDueDate() != null) {
            assignment.setDueDate(request.getDueDate());
        }

        assignment = assignmentRepository.save(assignment);
        log.info("Assignment updated: {}", id);

        return ApiResponse.setResponse(mapToResponse(assignment), 200);
    }

    @Transactional
    @Modifying
    @Override
    public void deleteAssignment(Long id, String teacherId) {
        if (!assignmentRepository.existsByIdAndTeacherEmployeeNumber(id, teacherId)) {
            throw new DataNotFoundException(ASSIGNMENT_NOT_FOUND);
        }

        // Delete associated resources
        List<String> resourcePaths = resourceRepository.getResourcePathByAssignmentId(id);
        resourcePaths.forEach(fileStorageService::deleteFile);
        resourceRepository.deleteByAssignmentId(id);

        assignmentRepository.deleteById(id);
        log.info("Assignment deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new DataNotFoundException(ASSIGNMENT_NOT_FOUND));
        return mapToResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<AssignmentResponse>> getAssignmentsByClassroom(Long classroomId, Pageable pageable) {
        Page<AssignmentResponse> assignments = assignmentRepository.findByClassroomId(classroomId, pageable).map(this::mapToResponse);
        return ApiResponse.setSuccess(PagedResponse.from(assignments));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<AssignmentResponse>> getAssignmentsByTeacher(String teacherId, Pageable pageable) {
        Page<AssignmentResponse> assignments = assignmentRepository.findByTeacherEmployeeNumber(teacherId, pageable)
                .map(this::mapToResponse);
        return ApiResponse.setSuccess(PagedResponse.from(assignments));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<AssignmentResponse>> getAssignmentsByClassroomAndSubject(Long classroomId, Long subjectId, Pageable pageable) {
        Page<AssignmentResponse> assignments = assignmentRepository.findByClassroomIdAndSubjectId(classroomId, subjectId, pageable)
                .map(this::mapToResponse);
        return ApiResponse.setSuccess(PagedResponse.from(assignments));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getUpcomingAssignments(Long classroomId) {
        return assignmentRepository.findUpcomingByClassroom(classroomId, LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public AssignmentResponse addResources(Long assignmentId, List<MultipartFile> files,
                                           List<String> resourceNames, List<String> resourceUrls,
                                           String teacherId) {
        Assignment assignment = assignmentRepository.findByIdAndTeacherEmployeeNumber(assignmentId, teacherId)
                .orElseThrow(() -> new DataNotFoundException(ASSIGNMENT_NOT_FOUND));


        int fileCount = 0;
        int urlCount = 0;

        // Process FILES
        if (files != null && !files.isEmpty()) {
            List<AssignmentResource> resources = files.stream().filter(it -> !it.isEmpty())
                    .map(it -> {
                        String resourcePath = fileStorageService.storeAssignmentResource(it, assignmentId);
                        AssignmentResource resource = new AssignmentResource();
                        resource.setAssignment(assignment);
                        resource.setResourceType(ResourceType.FILE);
                        resource.setResourceName(it.getOriginalFilename());
                        resource.setResourcePath(resourcePath);
                        resource.setFileSize(it.getSize());
                        resource.setMimeType(it.getContentType());
                        return resource;
                    }).toList();
            resourceRepository.saveAll(resources);
            fileCount = resources.size();
        }

        // Process URLs
        if (resourceUrls != null && !resourceUrls.isEmpty()) {
            for (int i = 0; i < resourceUrls.size(); i++) {
                String url = resourceUrls.get(i);
                if (url == null || url.isBlank()) {
                    continue;
                }

                String name = (resourceNames != null && i < resourceNames.size())
                        ? resourceNames.get(i)
                        : "Resource " + (i + 1);

                AssignmentResource resource = new AssignmentResource();
                resource.setAssignment(assignment);
                resource.setResourceType(ResourceType.URL);
                resource.setResourceName(name);
                resource.setResourcePath(url);
                resource.setFileSize(0L);
                resource.setMimeType("text/html");

                resourceRepository.save(resource);
                urlCount++;
            }
        }

        log.info("Added {} file(s) and {} URL(s) to assignment: {}", fileCount, urlCount, assignmentId);
        return mapToResponse(assignment);
    }

    @Transactional
    @Override
    public void deleteResource(Long assignmentId, Long resourceId, String teacherId) {
        if (!assignmentRepository.existsByIdAndTeacherEmployeeNumber(assignmentId, teacherId)) {
            throw new DataNotFoundException(ASSIGNMENT_NOT_FOUND);
        }

        AssignmentResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new DataNotFoundException("Resource not found"));

        if (!resource.getAssignment().getId().equals(assignmentId)) {
            throw new BadRequestException("Resource does not belong to this assignment");
        }

        fileStorageService.deleteFile(resource.getResourcePath());
        resourceRepository.delete(resource);
        log.info("Resource deleted: {}", resourceId);
    }

    private AssignmentResponse mapToResponse(Assignment assignment) {
        List<AssignmentResourceResponse> resources = resourceRepository.findByAssignmentId(assignment.getId())
                .stream()
                .map(this::mapResourceToResponse)
                .toList();

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .classroomId(assignment.getClassroom().getId())
                .classroomName(assignment.getClassroom().getName())
                .subjectId(assignment.getSubject().getId())
                .subjectName(assignment.getSubject().getName())
                .teacherId(assignment.getTeacher().getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate())
                .resources(resources)
                .createdAt(assignment.getCreatedDate())
                .updatedAt(assignment.getUpdatedDate())
                .build();
    }

    private AssignmentResourceResponse mapResourceToResponse(AssignmentResource resource) {
        return AssignmentResourceResponse.builder()
                .id(resource.getId())
                .resourceType(resource.getResourceType())
                .resourceName(resource.getResourceName())
                .resourcePath(resource.getResourcePath())
                .fileSize(resource.getFileSize())
                .mimeType(resource.getMimeType())
                .build();
    }
}
