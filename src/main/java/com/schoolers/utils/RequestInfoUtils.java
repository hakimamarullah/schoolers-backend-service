package com.schoolers.utils;

import com.schoolers.dto.request.BaseRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RequestInfoUtils {

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public void injectInfo(BaseRequest payload, HttpServletRequest servletRequest) {
        if (Objects.isNull(payload) || Objects.isNull(servletRequest)) {
            return;
        }
        payload.setClientIp(getClientIp(servletRequest));
        payload.setUserAgent(getUserAgent(servletRequest));
    }
}
