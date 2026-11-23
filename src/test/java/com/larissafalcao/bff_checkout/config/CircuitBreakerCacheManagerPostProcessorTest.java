package com.larissafalcao.bff_checkout.config;

import com.larissafalcao.bff_checkout.adapter.out.cache.CircuitBreakerCacheManager;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerCacheManagerPostProcessorTest {

    final CircuitBreakerCacheManagerPostProcessor postProcessor =
            new CircuitBreakerCacheManagerPostProcessor(CircuitBreaker.ofDefaults("test"));

    @Test
    void wrapsCacheManagerOnlyOnce() {
        Object wrapped = postProcessor.postProcessAfterInitialization(new ConcurrentMapCacheManager(), "cacheManager");

        assertThat(wrapped).isInstanceOf(CircuitBreakerCacheManager.class);
        assertThat(postProcessor.postProcessAfterInitialization(wrapped, "cacheManager")).isSameAs(wrapped);
    }

    @Test
    void leavesOtherBeansUntouched() {
        Object bean = new Object();

        assertThat(postProcessor.postProcessAfterInitialization(bean, "other")).isSameAs(bean);
    }
}
