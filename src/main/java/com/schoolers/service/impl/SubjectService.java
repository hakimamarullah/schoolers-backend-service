package com.schoolers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.request.CreateSubjectRequest;
import com.schoolers.dto.request.UpdateSubjectRequest;
import com.schoolers.dto.response.SubjectInfo;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.Subject;
import com.schoolers.repository.SubjectRepository;
import com.schoolers.service.ISubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        Subject.class,
        SubjectInfo.class,
        PagedResponse.class,
        Page.class,
        Pageable.class,
        CreateSubjectRequest.class,
        UpdateSubjectRequest.class,

})
public class SubjectService implements ISubjectService {

    private final SubjectRepository subjectRepository;

    private final ObjectMapper mapper;


    @Override
    public ApiResponse<PagedResponse<SubjectInfo>> getSubjectList(Pageable pageable) {
        Page<SubjectInfo> subjects = subjectRepository.findAll(pageable)
                .map(it -> mapper.convertValue(it, SubjectInfo.class));
        return ApiResponse.setSuccess(PagedResponse.from(subjects));
    }

    @Transactional
    @Override
    public ApiResponse<SubjectInfo> addSubject(CreateSubjectRequest payload) {
        Subject subject = mapper.convertValue(payload, Subject.class);
        subjectRepository.save(subject);
        return ApiResponse.setResponse( SubjectInfo.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .description(subject.getDescription())
                .build(), "Subject created", 201);
    }

    @Transactional
    @Override
    public void updateSubject(UpdateSubjectRequest payload) {
        Subject subject = subjectRepository.findById(payload.getId())
                .orElseThrow(() -> new DataNotFoundException("Subject not found"));
        subject.setName(payload.getName());
        subject.setCode(payload.getCode());
        subject.setDescription(payload.getDescription());
        subjectRepository.save(subject);
    }

    @Override
    public ApiResponse<SubjectInfo> getSubjectByCode(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new DataNotFoundException("Subject not found"));
        return ApiResponse.setSuccess(mapper.convertValue(subject, SubjectInfo.class));
    }
}
