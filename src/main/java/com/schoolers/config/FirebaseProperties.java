package com.schoolers.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "firebase")
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FirebaseProperties {


    private String type;

    private String projectId;

    private String privateKeyId;

    private String privateKey;

    private String clientEmail;

    private String clientId;

    private String authUri;

    private String tokenUri;

    private String authProviderX509CertUrl;

    private String clientX509CertUrl;

    private String universeDomain;

}
