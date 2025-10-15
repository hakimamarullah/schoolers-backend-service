package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.UpdateLocaleRequest;
import com.schoolers.enums.UserLocale;
import com.schoolers.repository.UserRepository;
import com.schoolers.service.ILocalizationService;
import com.schoolers.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        UserLocale.class,
        UpdateLocaleRequest.class
})
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final ILocalizationService localizationService;

    @Transactional
    @Override
    public ApiResponse<Void> updateLocale(UserLocale locale, String loginId) {
        int count = userRepository.updateLocaleByLoginId(loginId, locale.name().toLowerCase());
        if (count < 1) {
            return ApiResponse.setResponse(null, localizationService.getMessage("auth.user-not-found"), 404);
        }
        return ApiResponse.setResponse(null, localizationService.getMessage("locale.update-success"), 200);
    }
}
