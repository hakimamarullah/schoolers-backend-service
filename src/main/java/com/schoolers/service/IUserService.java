package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.enums.UserLocale;

public interface IUserService {

    ApiResponse<Void> updateLocale(UserLocale locale, String loginId);
}
