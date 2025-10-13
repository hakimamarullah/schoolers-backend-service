package com.schoolers.dto.event;

import com.google.firebase.messaging.BatchResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class FCMFailedTokenEvent {

    private List<String> tokens;

    private BatchResponse batchResponse;
}
