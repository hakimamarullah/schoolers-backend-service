package com.schoolers.dto.request;

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
public class CreateInformationRequest {
    private String title;
    private String body;
    private String bannerUri;
    private List<Long> targetUserIds;
    private List<String> targetClassroomIds;
    private List<String> targetRoles;
}
