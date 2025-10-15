package com.schoolers.service.impl;

import com.schoolers.service.ILocalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocalizationService implements ILocalizationService {

    private final MessageSource messageSource;
    @Override
    public String getMessage(String code, Object[] params) {
        return getMessageWithLocale(code, params, LocaleContextHolder.getLocale());
    }

    @Override
    public String getMessageWithLocale(String code, Object[] params, Locale locale) {
        return messageSource.getMessage(code, params, locale);
    }

    @Override
    public String getMessage(String code) {
        return getMessage(code, null);
    }

    @Override
    public String getMessageWithLocale(String code, Locale locale) {
        return getMessageWithLocale(code, null, locale);
    }
}
