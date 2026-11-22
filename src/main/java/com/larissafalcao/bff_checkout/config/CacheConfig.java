package com.larissafalcao.bff_checkout.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class CacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);
    private static final String CIRCUIT_BREAKER_NAME = "redis-cache";

    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

    @Bean
    static BeanPostProcessor circuitBreakerCacheManagerPostProcessor(Environment environment) {
        return new CircuitBreakerCacheManagerPostProcessor(
                cacheCircuitBreaker(CacheCircuitBreakerProperties.bind(environment)));
    }

    private static CircuitBreaker cacheCircuitBreaker(CacheCircuitBreakerProperties properties) {
        CircuitBreaker circuitBreaker = CircuitBreaker.of(CIRCUIT_BREAKER_NAME, properties.toCircuitBreakerConfig());
        circuitBreaker.getEventPublisher().onStateTransition(event ->
                log.warn("Circuit breaker do cache: {}", event.getStateTransition()));
        return circuitBreaker;
    }
}
