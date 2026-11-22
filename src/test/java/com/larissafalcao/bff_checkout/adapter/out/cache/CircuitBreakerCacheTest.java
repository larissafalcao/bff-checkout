package com.larissafalcao.bff_checkout.adapter.out.cache;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CircuitBreakerCacheTest {

    static final int MINIMUM_CALLS = 2;

    final Cache redis = mock(Cache.class);
    final CircuitBreaker circuitBreaker = CircuitBreaker.of("test", CircuitBreakerConfig.custom()
            .slidingWindowSize(MINIMUM_CALLS)
            .minimumNumberOfCalls(MINIMUM_CALLS)
            .waitDurationInOpenState(Duration.ofMinutes(1))
            .build());
    final CircuitBreakerCache cache = new CircuitBreakerCache(redis, circuitBreaker);

    @Test
    void delegatesWhileRedisIsHealthy() {
        when(redis.get("key")).thenReturn(new SimpleValueWrapper("value"));

        assertThat(cache.get("key")).extracting(Cache.ValueWrapper::get).isEqualTo("value");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void failuresAreRethrownUntilCircuitOpensThenRedisIsSkipped() {
        RedisConnectionFailureException redisDown = new RedisConnectionFailureException("down");
        when(redis.get(any())).thenThrow(redisDown);
        doThrow(redisDown).when(redis).put(any(), any());

        for (int call = 0; call < MINIMUM_CALLS; call++) {
            assertThatThrownBy(() -> cache.get("key")).isSameAs(redisDown);
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThat(cache.get("key")).isNull();
        cache.put("key", "value");
        verify(redis, times(MINIMUM_CALLS)).get(any());
        verify(redis, never()).put(any(), any());
    }

    @Test
    void behavesLikeTheRedisCacheWhileClosed() {
        when(redis.putIfAbsent("key", "value")).thenReturn(new SimpleValueWrapper("existing"));
        when(redis.evictIfPresent("key")).thenReturn(true);
        doReturn(CompletableFuture.completedFuture("value")).when(redis).retrieve("key");

        assertThat(cache.putIfAbsent("key", "value")).extracting(Cache.ValueWrapper::get).isEqualTo("existing");
        assertThat(cache.evictIfPresent("key")).isTrue();
        assertThat(cache.retrieve("key")).succeedsWithin(Duration.ZERO).isEqualTo("value");
    }

    @Test
    void asyncFailuresAlsoOpenTheCircuit() {
        RedisConnectionFailureException redisDown = new RedisConnectionFailureException("down");
        doReturn(CompletableFuture.failedFuture(redisDown)).when(redis).retrieve(any());

        for (int call = 0; call < MINIMUM_CALLS; call++) {
            assertThat(cache.retrieve("key")).isCompletedExceptionally();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(cache.retrieve("key")).isNull();
        assertThat(cache.retrieve("key", () -> CompletableFuture.completedFuture("loaded"))).isCompletedWithValue("loaded");
    }

    @Test
    void openCircuitLoadsValueDirectly() {
        circuitBreaker.transitionToOpenState();

        assertThat(cache.get("key", () -> "loaded")).isEqualTo("loaded");
        verify(redis, never()).get(any(), any(Callable.class));
    }
}
