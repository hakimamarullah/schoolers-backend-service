package com.schoolers.service;


import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.request.CreateSubjectRequest;
import com.schoolers.dto.request.UpdateSubjectRequest;
import com.schoolers.dto.response.SubjectInfo;
import org.springframework.data.domain.Pageable;

public interface ISubjectService {

    ApiResponse<PagedResponse<SubjectInfo>> getSubjectList(Pageable pageable);

    ApiResponse<SubjectInfo> addSubject(CreateSubjectRequest payload);

    void updateSubject(UpdateSubjectRequest payload);

    ApiResponse<SubjectInfo>  getSubjectByCode(String code);
}
