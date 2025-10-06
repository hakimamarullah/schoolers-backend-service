package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.response.SimpleClassroomInfo;

import java.util.List;

public interface IClassroomService {

    ApiResponse<List<SimpleClassroomInfo>> getClassrooms();
}
