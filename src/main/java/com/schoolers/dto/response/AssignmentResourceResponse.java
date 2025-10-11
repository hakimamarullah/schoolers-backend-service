package com.schoolers.dto.response;

import com.schoolers.enums.ResourceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AssignmentResourceResponse {

    private Long id;
    private ResourceType resourceType;
    private String resourceName;
    private String resourcePath;
    private Long fileSize;
    private String mimeType;
}
