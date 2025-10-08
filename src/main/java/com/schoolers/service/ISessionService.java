package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.ValidateSessionRequest;
import com.schoolers.dto.response.ValidateSessionResponse;

public interface ISessionService {

    ApiResponse<ValidateSessionResponse> validateSession(ValidateSessionRequest payload);
}
