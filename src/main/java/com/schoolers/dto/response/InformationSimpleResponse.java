package com.schoolers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformationSimpleResponse {
    private Long id;
    private String title;
    private String body;
    private String bannerUri;
    private String createdAt;
    private String authorName;
    private Boolean hasRead;
}
