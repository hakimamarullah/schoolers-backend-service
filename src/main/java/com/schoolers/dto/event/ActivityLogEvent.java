package com.schoolers.dto.event;

import com.schoolers.enums.ActivityType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ActivityLogEvent {

    private ActivityType activityType;


    private Long entityId;

    private String entityName;


    private String description;


    private String ipAddress;

    private String userAgent;
}
