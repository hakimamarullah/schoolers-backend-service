package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.CreateClassroomRequest;
import com.schoolers.dto.request.UpdateClassroomRequest;
import com.schoolers.dto.response.ClassroomInfo;
import com.schoolers.dto.response.SimpleClassroomInfo;

import java.util.List;

public interface IClassroomService {

    ApiResponse<List<SimpleClassroomInfo>> getClassrooms();

    ApiResponse<ClassroomInfo> addClassroom(CreateClassroomRequest payload);

    ApiResponse<ClassroomInfo> getClassroomById(Long id);

    void updateClassroom(UpdateClassroomRequest payload);
}
