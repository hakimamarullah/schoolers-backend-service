package com.schoolers.dto.projection;

import java.time.Instant;

public interface InformationDTO {
    Long getId();
    String getTitle();
    String getBody();
    String getBannerUri();
    Instant getCreatedAt();
    String getAuthorName();
    Boolean getHasRead();
}