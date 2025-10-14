package com.schoolers.dto.event;

import com.schoolers.dto.request.CreateInformationRequest;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class NewInformationEvent {

    private Set<Long> userId;
    private Set<Long> classroomId;
    private Set<String> role;
    private Long informationId;
    private String title;


    public NewInformationEvent(CreateInformationRequest payload, Long id, String title) {
        this.userId = new HashSet<>(Optional.ofNullable(payload.getTargetUserIds())
                .orElse(new ArrayList<>()));
        this.classroomId = Optional.ofNullable(payload.getTargetClassroomIds())
                .orElse(new ArrayList<>())
                .stream()
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        this.role = Optional.ofNullable(payload.getTargetRoles())
                .orElse(new ArrayList<>())
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        this.informationId = id;
        this.title = title;
    }
}
