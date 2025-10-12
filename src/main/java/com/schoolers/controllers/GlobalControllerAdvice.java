package com.schoolers.controllers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.schoolers.annotations.LogResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.exceptions.ApiException;
import com.schoolers.service.ILocalizationService;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestControllerAdvice
@Slf4j
@LogResponse
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ObjectMapper mapper;

    private final ILocalizationService localizationService;


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("[INVALID ARGUMENTS]: {}", ex.getMessage(), ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(localizationService.getMessage("error.invalid-arguments"));
        response.setData(errors);
        return response.toResponseEntity();
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleAccessExceptions(AuthorizationDeniedException ex) {
        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setCode(403);
        response.setMessage(localizationService.getMessage("error.access-denied"));
        return response.toResponseEntity();
    }

    @ExceptionHandler({NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Map<String, String>>> handleNoResourceFoundExceptions(NoResourceFoundException ex) {
        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setCode(404);
        response.setData(Map.of("path", ex.getResourcePath()));
        response.setMessage(localizationService.getMessage("error.not-found"));
        return response.toResponseEntity();
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ApiResponse<Map<String, String>>> handleNoEntityFoundExceptions(EntityNotFoundException ex) {
        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setCode(404);
        response.setMessage(localizationService.getMessage("error.not-found"));
        return response.toResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> internalServerError(Exception ex) {
        log.error("[INTERNAL SERVER ERROR]: {}", ex.getMessage(), ex);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(500);
        response.setMessage(localizationService.getMessage("error.unknown-error"));

        String causeClassName = Optional.ofNullable(ex.getCause())
                .map(Throwable::getClass)
                .map(Class::getCanonicalName)
                .orElse(null);
        response.setData(causeClassName);
        return response.toResponseEntity();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> httpMessageNotReadableError(HttpMessageNotReadableException ex) {
        log.error("[MISSING REQUEST BODY]: {}", ex.getMessage(), ex);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(localizationService.getMessage("error.parsing-error"));

        String causeClassName = Optional.ofNullable(ex.getCause())
                .map(Throwable::getClass)
                .map(Class::getCanonicalName)
                .orElse(null);
        response.setData(causeClassName);
        return response.toResponseEntity();
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> methodNotSupportedExHandler(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(405);
        response.setMessage(ex.getMessage());
        response.setData(req.getRequestURI());
        return response.toResponseEntity();
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ApiResponse<String>> dataIntegrityViolationHandler(DataIntegrityViolationException ex) {
        log.error(ex.getMessage(), ex);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(localizationService.getMessage("error.data-integrity"));
        response.setData(ex.getMessage());

        return response.toResponseEntity();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<String>> missingServletRequestParameterException(MissingServletRequestParameterException ex) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(localizationService.getMessage("error.invalid-arguments"));
        response.setData(ex.getParameterName());

        return response.toResponseEntity();
    }


    @ExceptionHandler({InvalidFormatException.class, JsonParseException.class, DateTimeParseException.class, InvalidFormatException.class})
    public ResponseEntity<ApiResponse<String>> jsonExceptionHandler(Exception ex) {
        log.error(ex.getMessage(), ex);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(localizationService.getMessage("error.parsing-error"));
        response.setData(ex.getClass().getCanonicalName());

        return response.toResponseEntity();
    }

    @ExceptionHandler({ApiException.class})
    public ResponseEntity<ApiResponse<String>> apiExceptionHandler(ApiException ex) {
        log.error(ex.getMessage());
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(ex.getHttpCode());
        response.setMessage(ex.getMessage());
        response.setData(ex.getClass().getCanonicalName());

        return response.toResponseEntity();
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ApiResponse<String>> badCredentialExceptionHandler(BadCredentialsException ex) {
        log.error(ex.getMessage());
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(401);
        response.setMessage(ex.getMessage());
        response.setData(ex.getClass().getCanonicalName());

        return response.toResponseEntity();
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResponseEntity<ApiResponse<String>> fileSizeExceededExceptionHandler(MaxUploadSizeExceededException ex) {
        log.error(ex.getMessage());
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(localizationService.getMessage("error.file-size-exceeded"));
        response.setData(ex.getClass().getCanonicalName());

        return response.toResponseEntity();
    }

    @ExceptionHandler({FeignException.class})
    public ResponseEntity<ApiResponse<String>> feignExceptionHandler(FeignException ex) throws JsonProcessingException {
        log.error(ex.getMessage());
        if (Objects.isNull(ex.contentUTF8()) || ex.contentUTF8().isBlank()) {
            return internalServerError(ex);
        }
        Map<String, Object> converted =  mapper.readValue(ex.contentUTF8(), new TypeReference<>() {
        });
        ApiResponse<String> res = new ApiResponse<>();
        if (!Objects.isNull(converted.get("status"))) {
            res.setCode((Integer) converted.get("status"));
            res.setMessage((String) converted.get("error"));
            res.setData((String) converted.get("path"));
        } else {
            res = mapper.convertValue(converted, new TypeReference<>() {
            });
        }
        res.setMessage(suppressMessage(res.getMessage()));
        return res.toResponseEntity();
    }

    private String suppressMessage(String message) {
        return Optional.ofNullable(message)
                .map(it -> it.substring(0, Math.min(message.length(), 150)))
                .orElse(localizationService.getMessage("error.unknown-error"));
    }


}
