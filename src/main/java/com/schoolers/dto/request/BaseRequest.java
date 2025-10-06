package com.schoolers.dto.request;

import lombok.Data;

@Data
public class BaseRequest {

    private String clientIp;
    private String userAgent;
    private Long userId;
}
