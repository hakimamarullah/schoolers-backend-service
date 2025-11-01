package com.schoolers.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.schoolers.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
@Primary
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {


    private final ObjectMapper objectMapper;



    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ApiResponse<String> error = new ApiResponse<>();
        error.setMessage("You are not authenticated, please login to get access token");
        error.setCode(HttpServletResponse.SC_UNAUTHORIZED);
        error.setData("Unauthorized");
        OutputStream out = response.getOutputStream();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        objectMapper.writeValue(out, error);
        out.flush();
    }
}