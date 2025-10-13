package com.schoolers.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InformationDetailResponse {
    private Long id;
    private String title;
    private String body;
    private String bannerUri;
    private String createdAt;
    private String updatedAt;
    private AuthorDto author;
    private List<TargetDto> targets;
    private Boolean hasRead;
    private String readAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private String id;
        private String name;
        private String email;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetDto {
        private String type; // USER, CLASSROOM, ROLE
        private String value;
    }
}