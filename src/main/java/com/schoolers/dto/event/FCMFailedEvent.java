package com.schoolers.dto.event;

import com.google.firebase.messaging.MessagingErrorCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class FCMFailedEvent {

    private String token;

    private MessagingErrorCode errorCode;
}
