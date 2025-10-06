package com.schoolers.feign.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FeignBasicConfig {

    private final OpenTelemetry openTelemetry;

    @Bean
    Logger.Level loggerInfo() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor tracingFeignInterceptor() {
        return requestTemplate -> {
            try {
                TextMapSetter<RequestTemplate> setter = (template, key, value) -> {
                    if (template != null && key != null && value != null) {
                        template.header(key, value);
                    }
                };


                // Inject context into Feign request headers
                openTelemetry.getPropagators()
                        .getTextMapPropagator()
                        .inject(Context.current(), requestTemplate, setter);
            } catch (Exception e) {
                log.warn("Failed to inject context into Feign request headers {}", e.getMessage());
            }

        };
    }
}
