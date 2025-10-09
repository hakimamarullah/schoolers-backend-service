package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.response.HomepageResponse;

import java.time.LocalDate;

public interface IStudentHomePageService {

    ApiResponse<HomepageResponse> getStudentHomepage(String studentNumber, LocalDate date);

}
