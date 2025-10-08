package com.schoolers.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolers.dto.event.ActivityLogEvent;
import com.schoolers.models.ActivityLog;
import com.schoolers.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityLogListener {

    private final ActivityLogRepository activityLogRepository;

    private final ObjectMapper mapper;

    @EventListener(ActivityLogEvent.class)
    @Async
    public void logActivity(ActivityLogEvent event) {
        log.info("Record activity {}", event);
        var activityLog = mapper.convertValue(event, ActivityLog.class);
        activityLogRepository.save(activityLog);
        log.info("Activity recorded successfully -> {}", activityLog.getId());
    }
}
