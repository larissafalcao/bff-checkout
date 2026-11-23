package com.larissafalcao.bff_checkout.adapter.out.cache;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CircuitBreakerCacheManager implements CacheManager {

    private final CacheManager delegate;
    private final CircuitBreaker circuitBreaker;
    private final Map<String, Cache> protectedCaches = new ConcurrentHashMap<>();

    public CircuitBreakerCacheManager(CacheManager delegate, CircuitBreaker circuitBreaker) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = delegate.getCache(name);
        if (cache == null) {
            return null;
        }
        return protectedCaches.computeIfAbsent(name, ignored -> new CircuitBreakerCache(cache, circuitBreaker));
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
