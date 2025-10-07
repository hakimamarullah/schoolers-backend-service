package com.schoolers.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.response.SimpleClassroomInfo;
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
}
