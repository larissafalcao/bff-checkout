package com.larissafalcao.bff_checkout.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CacheCircuitBreakerPropertiesTest {

    @Test
    void bindsLimitsFromEnvironmentIntoCircuitBreakerConfig() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("checkout.cache.circuit-breaker.sliding-window-size", "20")
                .withProperty("checkout.cache.circuit-breaker.minimum-number-of-calls", "7")
                .withProperty("checkout.cache.circuit-breaker.failure-rate-threshold", "25")
                .withProperty("checkout.cache.circuit-breaker.wait-duration-in-open-state", "1m")
                .withProperty("checkout.cache.circuit-breaker.permitted-number-of-calls-in-half-open-state", "3");

        CircuitBreakerConfig config = CacheCircuitBreakerProperties.bind(environment).toCircuitBreakerConfig();

        assertThat(config.getSlidingWindowSize()).isEqualTo(20);
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(7);
        assertThat(config.getFailureRateThreshold()).isEqualTo(25f);
        assertThat(config.getWaitIntervalFunctionInOpenState().apply(1)).isEqualTo(Duration.ofMinutes(1).toMillis());
        assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
    }

    @Test
    void missingConfigurationFailsStartup() {
        assertThatThrownBy(() -> CacheCircuitBreakerProperties.bind(new MockEnvironment()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(CacheCircuitBreakerProperties.PREFIX);
    }
}
