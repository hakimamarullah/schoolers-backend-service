package com.schoolers.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class BiometricRegistrationResponse {

    private Long credentialId;
    private String publicKeyHash;
}
