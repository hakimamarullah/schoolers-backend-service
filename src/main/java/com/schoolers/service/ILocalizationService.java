package com.schoolers.service;

public interface ILocalizationService {

    String getMessage(String code, Object[] params);

    String getMessage(String code);
}
