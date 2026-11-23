package com.larissafalcao.bff_checkout.adapter.out.cache;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Decorator que protege um cache com circuit breaker. Com o circuito aberto, a leitura vira cache miss e a escrita é
 * ignorada na hora, sem esperar o timeout do Redis. Com o circuito fechado, as falhas são repassadas para o
 * {@link org.springframework.cache.interceptor.CacheErrorHandler}, que as registra e segue sem cache.
 */
public class CircuitBreakerCache implements Cache {

    private final Cache delegate;
    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerCache(Cache delegate, CircuitBreaker circuitBreaker) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        return protect(() -> delegate.get(key), () -> null);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return protect(() -> delegate.get(key, type), () -> null);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return protect(() -> delegate.get(key, valueLoader), () -> load(key, valueLoader));
    }

    @Override
    public CompletableFuture<?> retrieve(Object key) {
        return protectAsync(() -> delegate.retrieve(key), () -> null);
    }

    @Override
    public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader) {
        return protectAsync(() -> delegate.retrieve(key, valueLoader), valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        protect(() -> delegate.put(key, value));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return protect(() -> delegate.putIfAbsent(key, value), () -> null);
    }

    @Override
    public void evict(Object key) {
        protect(() -> delegate.evict(key));
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return protect(() -> delegate.evictIfPresent(key), () -> false);
    }

    @Override
    public void clear() {
        protect(delegate::clear);
    }

    @Override
    public boolean invalidate() {
        return protect(delegate::invalidate, () -> false);
    }

    private void protect(Runnable cacheCall) {
        protect(() -> {
            cacheCall.run();
            return null;
        }, () -> null);
    }

    private <T> T protect(Supplier<T> cacheCall, Supplier<T> whenOpen) {
        if (!circuitBreaker.tryAcquirePermission()) {
            return whenOpen.get();
        }
        long start = circuitBreaker.getCurrentTimestamp();
        try {
            T result = cacheCall.get();
            recordSuccess(start);
            return result;
        } catch (RuntimeException failure) {
            recordFailure(start, failure);
            throw failure;
        }
    }

    /** No modo assíncrono a falha chega pelo future, então o resultado é registrado quando ele termina. */
    private <T> CompletableFuture<T> protectAsync(Supplier<CompletableFuture<T>> cacheCall,
                                                  Supplier<CompletableFuture<T>> whenOpen) {
        if (!circuitBreaker.tryAcquirePermission()) {
            return whenOpen.get();
        }
        long start = circuitBreaker.getCurrentTimestamp();
        CompletableFuture<T> future;
        try {
            future = cacheCall.get();
        } catch (RuntimeException failure) {
            recordFailure(start, failure);
            throw failure;
        }
        if (future == null) {
            recordSuccess(start);
            return null;
        }
        return future.whenComplete((value, failure) -> {
            if (failure == null) {
                recordSuccess(start);
            } else {
                recordFailure(start, failure);
            }
        });
    }

    private void recordSuccess(long start) {
        circuitBreaker.onSuccess(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit());
    }

    private void recordFailure(long start, Throwable failure) {
        circuitBreaker.onError(circuitBreaker.getCurrentTimestamp() - start, circuitBreaker.getTimestampUnit(), failure);
    }

    private static <T> T load(Object key, Callable<T> valueLoader) {
        try {
            return valueLoader.call();
        } catch (Exception exception) {
            throw new ValueRetrievalException(key, valueLoader, exception);
        }
    }
}
