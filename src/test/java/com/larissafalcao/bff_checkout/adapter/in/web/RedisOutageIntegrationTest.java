package com.larissafalcao.bff_checkout.adapter.in.web;

import com.larissafalcao.bff_checkout.adapter.out.cache.CircuitBreakerCacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class RedisOutageIntegrationTest {

    // O ciclo de vida (start/stop) é gerenciado pela extensão @Testcontainers, não por try-with-resources.
    @SuppressWarnings("resource")
    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    static final int PORT = TestSocketUtils.findAvailableTcpPort();
    static final int CLOSED_REDIS_PORT = TestSocketUtils.findAvailableTcpPort();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> PORT);
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> CLOSED_REDIS_PORT);
    }

    @Autowired MockMvc mvc;
    @Autowired CacheManager cacheManager;

    @Test
    void preCheckoutFallsBackToCatalogServicesWhenRedisIsDown() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post("/operations/pre-checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"products\": [{\"slug\": \"premium\"}]}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products[0].slug").value("premium"))
                    .andExpect(jsonPath("$.paymentMethods.length()").value(3));
        }
    }

    @Test
    void circuitOpensAndRedisStopsBeingCalled() throws Exception {
        preCheckoutFallsBackToCatalogServicesWhenRedisIsDown();

        Cache products = cacheManager.getCache("products");

        assertThat(cacheManager).isInstanceOf(CircuitBreakerCacheManager.class);
        assertThat(products).isNotNull();
        assertThat(products.get(SimpleKey.EMPTY)).as("circuito aberto: miss sem tocar no Redis").isNull();
    }

    @Test
    void instanceStaysReadyWithoutRedis() throws Exception {
        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
