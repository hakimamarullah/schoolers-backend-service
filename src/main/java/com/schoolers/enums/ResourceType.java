package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum ResourceType {
    FILE, URL;

    @JsonCreator
    public ResourceType fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(ResourceType::valueOf)
                .orElse(null);
    }
}
