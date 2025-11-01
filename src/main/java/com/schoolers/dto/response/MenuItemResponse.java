package com.schoolers.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class MenuItemResponse {

    private Long id;
    private String title;
    private String route;
    private String iconName;
    private String badgeText;
    private Boolean isEnabled;
}
