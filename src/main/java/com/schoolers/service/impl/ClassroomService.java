package com.schoolers.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.CreateClassroomRequest;
import com.schoolers.dto.request.UpdateClassroomRequest;
import com.schoolers.dto.response.ClassroomInfo;
import com.schoolers.dto.response.SimpleClassroomInfo;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.Classroom;
import com.schoolers.repository.ClassroomRepository;
import com.schoolers.service.IClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        SimpleClassroomInfo.class
})
public class ClassroomService implements IClassroomService {

    private final ClassroomRepository classroomRepository;

    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<SimpleClassroomInfo>> getClassrooms() {
        List<SimpleClassroomInfo> simpleClassroomInfos = mapper.convertValue(classroomRepository.findAll(), new TypeReference<>() {
        });
        return ApiResponse.setSuccess(simpleClassroomInfos);
    }

    @Transactional
    @Override
    public ApiResponse<ClassroomInfo> addClassroom(CreateClassroomRequest payload) {
        Classroom classroom = mapper.convertValue(payload, Classroom.class);
        classroomRepository.save(classroom);

        return ApiResponse.setResponse(ClassroomInfo.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .grade(classroom.getGrade())
                .academicYear(classroom.getAcademicYear())
                .build(), "Classroom created", 201);
    }

    @Override
    public ApiResponse<ClassroomInfo> getClassroomById(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Classroom not found"));
        ClassroomInfo classroomInfo = mapper.convertValue(classroom, ClassroomInfo.class);
        return ApiResponse.setSuccess(classroomInfo);
    }

    @Transactional
    @Override
    public void updateClassroom(UpdateClassroomRequest payload) {
        Classroom classroom = classroomRepository.findById(payload.getId())
                .orElseThrow(() -> new DataNotFoundException("Classroom not found"));
        classroom.setName(payload.getName());
        classroom.setGrade(payload.getGrade());
        classroom.setAcademicYear(payload.getAcademicYear());
        classroomRepository.save(classroom);
    }
}
