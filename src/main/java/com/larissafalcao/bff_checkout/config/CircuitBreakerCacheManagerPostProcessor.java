package com.larissafalcao.bff_checkout.config;

import com.larissafalcao.bff_checkout.adapter.out.cache.CircuitBreakerCacheManager;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.CacheManager;

class CircuitBreakerCacheManagerPostProcessor implements BeanPostProcessor {

    private final CircuitBreaker circuitBreaker;

    CircuitBreakerCacheManagerPostProcessor(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof CacheManager cacheManager && !(bean instanceof CircuitBreakerCacheManager)) {
            return new CircuitBreakerCacheManager(cacheManager, circuitBreaker);
        }
        return bean;
    }
}
