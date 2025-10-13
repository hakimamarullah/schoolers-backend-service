package com.schoolers.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubscribeTopicRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Topic is required")
    private String topic;
}
