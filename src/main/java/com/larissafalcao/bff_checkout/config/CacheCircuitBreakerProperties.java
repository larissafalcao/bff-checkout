package com.larissafalcao.bff_checkout.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.time.Duration;

public record CacheCircuitBreakerProperties(
        int slidingWindowSize,
        int minimumNumberOfCalls,
        float failureRateThreshold,
        Duration waitDurationInOpenState,
        int permittedNumberOfCallsInHalfOpenState) {

    static final String PREFIX = "checkout.cache.circuit-breaker";

    /** Usa o Binder direto porque um BeanPostProcessor sobe antes do binding de @ConfigurationProperties. */
    static CacheCircuitBreakerProperties bind(Environment environment) {
        return Binder.get(environment)
                .bind(PREFIX, CacheCircuitBreakerProperties.class)
                .orElseThrow(() -> new IllegalStateException("Configuração ausente: " + PREFIX));
    }

    CircuitBreakerConfig toCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(waitDurationInOpenState)
                .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                .build();
    }
}
