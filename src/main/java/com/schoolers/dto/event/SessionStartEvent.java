package com.schoolers.dto.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SessionStartEvent {

    private Long sessionId;
}
