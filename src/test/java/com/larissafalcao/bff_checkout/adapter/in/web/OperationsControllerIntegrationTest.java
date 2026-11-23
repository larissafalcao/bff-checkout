package com.larissafalcao.bff_checkout.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import com.larissafalcao.bff_checkout.application.port.out.CartRepositoryPort;
import com.larissafalcao.bff_checkout.application.port.out.ConcurrentCartUpdateException;
import com.larissafalcao.bff_checkout.domain.model.Cart;
import com.larissafalcao.bff_checkout.domain.model.CartStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class OperationsControllerIntegrationTest {

    // O ciclo de vida (start/stop) é gerenciado pela extensão @Testcontainers, não por try-with-resources.
    @SuppressWarnings("resource")
    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @SuppressWarnings("resource")
    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static final int PORT = TestSocketUtils.findAvailableTcpPort();
    static final String GENERATED_ID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> PORT);
    }

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CartRepositoryPort cartRepository;
    @Autowired CacheManager cacheManager;

    @Test
    void preCheckoutReturnsCartForPaymentScreen() throws Exception {
        preCheckout("premium")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").isString())
                .andExpect(jsonPath("$.products[0].slug").value("premium"))
                .andExpect(jsonPath("$.products[0].name").value("Plano Premium"))
                .andExpect(jsonPath("$.products[0].description").value("Assinatura mensal com catálogo completo"))
                .andExpect(jsonPath("$.products[0].price").value(39.90))
                .andExpect(jsonPath("$.products[0].currency").value("BRL"))
                .andExpect(jsonPath("$.paymentMethods[*].name")
                        .value(contains("Cartão de Crédito", "Cartão de Débito", "Pix")))
                .andExpect(jsonPath("$.paymentMethods[0].requiresCard").doesNotExist());
    }

    @Test
    void catalogsAreServedFromRedisCache() throws Exception {
        preCheckout("premium").andExpect(status().isOk());

        assertThat(cacheManager.getCache("products").get(SimpleKey.EMPTY)).isNotNull();
        assertThat(cacheManager.getCache("paymentMethods").get(SimpleKey.EMPTY)).isNotNull();
        preCheckout("familia").andExpect(status().isOk()).andExpect(jsonPath("$.products[0].price").value(59.90));
    }

    @Test
    void unknownProductReturnsFrontendError() throws Exception {
        preCheckout("ouro")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.displayType").value("BLOCK"))
                .andExpect(jsonPath("$.frontendError.code").value("P-2001"))
                .andExpect(jsonPath("$.frontendError.primaryButton.label").isString());
    }

    @Test
    void duplicateProductReturnsFrontendError() throws Exception {
        mvc.perform(post("/operations/pre-checkout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"products\": [{\"slug\": \"premium\"}, {\"slug\": \"premium\"}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_PRODUCT"))
                .andExpect(jsonPath("$.frontendError.code").value("P-2002"));
    }

    @Test
    void emptyProductListIsInvalidRequest() throws Exception {
        mvc.perform(post("/operations/pre-checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"products\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    void cardCheckoutSucceedsThenDuplicateIsRejected() throws Exception {
        String cartId = createCart();

        checkout(cartId, card("Cartão de Crédito", "4111111111114242"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.paidAt").isString())
                .andExpect(jsonPath("$.payment.method").value("Cartão de Crédito"))
                .andExpect(jsonPath("$.payment.lastDigits").value("4242"))
                .andExpect(jsonPath("$.payment.pixCode").doesNotExist())
                .andExpect(jsonPath("$.payment.amount").value(39.90))
                .andExpect(jsonPath("$.payment.currency").value("BRL"))
                .andExpect(jsonPath("$.products[0].slug").value("premium"))
                .andExpect(jsonPath("$.products[0].name").value("Plano Premium"))
                .andExpect(jsonPath("$.products[0].price").value(39.90));

        checkout(cartId, card("Cartão de Crédito", "4111111111114242"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CART_ALREADY_PAID"))
                .andExpect(jsonPath("$.cartId").value(cartId));

        var row = jdbc.queryForMap("select * from cart where id = ?", UUID.fromString(cartId));
        assertThat(row.values()).noneMatch(value -> String.valueOf(value).contains("4111111111114242"));
        assertThat(row.values()).noneMatch(value -> "987".equals(String.valueOf(value)));
        assertThat(row.get("last_digits")).isEqualTo("4242");
    }

    @Test
    void pixCheckoutReturnsPixCodeInsteadOfLastDigits() throws Exception {
        String cartId = createCart();

        checkout(cartId, "{\"name\": \"Pix\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payment.method").value("Pix"))
                .andExpect(jsonPath("$.payment.pixCode").isString())
                .andExpect(jsonPath("$.payment.lastDigits").doesNotExist());
    }

    @Test
    void cardEndingIn0002ReturnsDeclinedErrorAndAllowsRetry() throws Exception {
        String cartId = createCart();

        checkout(cartId, card("Cartão de Crédito", "4111111111110002"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.displayType").value("BLOCK"))
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_DECLINED"))
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.frontendError.code").value("P-1001"))
                .andExpect(jsonPath("$.frontendError.title").value("Pagamento não aprovado"))
                .andExpect(jsonPath("$.frontendError.description").value(
                        "Não foi possível concluir o pagamento. Tente novamente ou escolha outro meio."))
                .andExpect(jsonPath("$.frontendError.primaryButton.label").value("Tentar novamente"))
                .andExpect(jsonPath("$.frontendError.primaryButton.url").value("RETRY"))
                .andExpect(jsonPath("$.frontendError.secondaryButton.label").value("Trocar meio de pagamento"))
                .andExpect(jsonPath("$.frontendError.secondaryButton.url").value("/checkout/pagamento"));

        checkout(cartId, card("Cartão de Crédito", "4111111111114242")).andExpect(status().isOk());
    }

    @Test
    void paymentMethodNotOfferedForCartIsRejected() throws Exception {
        String cartId = createCart();

        checkout(cartId, "{\"name\": \"Boleto\"}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.displayType").value("INTERVENTION"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAYMENT_METHOD"))
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.frontendError.code").value("P-1002"))
                .andExpect(jsonPath("$.frontendError.primaryButton.url").value("/checkout/pagamento"));
    }

    @Test
    void invalidPaymentDataIsRejected() throws Exception {
        String cartId = createCart();

        checkout(cartId, """
                {"name": "Pix",
                 "card": {"holderName": "Ana", "number": "4111111111114242", "expiry": "12/35", "cvv": "123"}}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.displayType").value("INTERVENTION"))
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAYMENT_DATA"))
                .andExpect(jsonPath("$.frontendError.code").value("P-1003"));
        checkout(cartId, "{\"name\": \"Cartão de Débito\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAYMENT_DATA"));
        checkout(cartId, """
                {"name": "Cartão de Débito",
                 "card": {"holderName": "Ana", "number": "4111111111114242", "expiry": "01/20", "cvv": "123"}}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAYMENT_DATA"));
    }

    @Test
    void unknownCartReturnsFrontendError() throws Exception {
        checkout(UUID.randomUUID().toString(), "{\"name\": \"Pix\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CART_NOT_FOUND"))
                .andExpect(jsonPath("$.frontendError.code").value("P-3001"));
    }

    @Test
    void malformedRequestsUseSameErrorShape() throws Exception {
        mvc.perform(post("/operations/checkout").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.displayType").value("INTERVENTION"))
                .andExpect(jsonPath("$.frontendError.title").isString());
        mvc.perform(post("/operations/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cartId\": \"abc\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        mvc.perform(get("/operations/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.frontendError.code").isString());
    }

    @Test
    void concurrentCheckoutsChargeOnlyOnce() throws Exception {
        String cartId = createCart();
        CountDownLatch start = new CountDownLatch(1);
        Callable<Integer> pay = () -> {
            start.await();
            return checkout(cartId, "{\"name\": \"Pix\"}").andReturn().getResponse().getStatus();
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Integer>> results = List.of(pool.submit(pay), pool.submit(pay));
            start.countDown();
            assertThat(List.of(results.get(0).get(), results.get(1).get())).containsExactlyInAnyOrder(200, 409);
        } finally {
            pool.shutdown();
        }
        assertThat(jdbc.queryForObject("select status from cart where id = ?", String.class, UUID.fromString(cartId)))
                .isEqualTo("APPROVED");
    }

    @Test
    void concurrentReservationIsRejectedByOptimisticLock() throws Exception {
        UUID cartId = UUID.fromString(createCart());
        Cart first = cartRepository.findById(cartId).orElseThrow();
        Cart stale = cartRepository.findById(cartId).orElseThrow();   // mesma versão, lida "ao mesmo tempo"

        first.startPayment(Instant.now(), Duration.ofMinutes(2));
        cartRepository.save(first);

        stale.startPayment(Instant.now(), Duration.ofMinutes(2));
        assertThatThrownBy(() -> cartRepository.save(stale)).isInstanceOf(ConcurrentCartUpdateException.class);
        assertThat(cartRepository.findById(cartId).orElseThrow().getStatus()).isEqualTo(CartStatus.PROCESSING);
    }

    @Test
    void healthProbesAreUp() throws Exception {
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void requestIdIsEchoedOrGenerated() throws Exception {
        mvc.perform(get("/actuator/health").header("X-Request-Id", "abc-123"))
                .andExpect(header().string("X-Request-Id", "abc-123"));
        mvc.perform(get("/actuator/health").header("X-Request-Id", "id com espaço e quebra\nde linha"))
                .andExpect(header().string("X-Request-Id", matchesPattern(GENERATED_ID)));
        mvc.perform(get("/actuator/health"))
                .andExpect(header().string("X-Request-Id", matchesPattern(GENERATED_ID)));
    }

    static final String CHECKOUT_422_EXAMPLES =
            "$.paths['/operations/checkout'].post.responses['422'].content['application/json'].examples";

    @Test
    void openApiDocumentsOnlyBffEndpointsWithErrorContract() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("BFF de Checkout"))
                .andExpect(jsonPath("$.paths['/operations/pre-checkout'].post").exists())
                .andExpect(jsonPath("$.paths['/operations/checkout'].post").exists())
                .andExpect(jsonPath("$.paths['/mock/products']").doesNotExist())
                .andExpect(jsonPath(CHECKOUT_422_EXAMPLES + ".PAYMENT_DECLINED").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse").exists());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    private ResultActions preCheckout(String slug) throws Exception {
        return mvc.perform(post("/operations/pre-checkout").contentType(MediaType.APPLICATION_JSON)
                .content("{\"products\": [{\"slug\": \"" + slug + "\"}]}"));
    }

    private String createCart() throws Exception {
        String body = preCheckout("premium").andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.cartId");
    }

    private ResultActions checkout(String cartId, String paymentMethodJson) throws Exception {
        return mvc.perform(post("/operations/checkout").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cartId\": \"" + cartId + "\", \"paymentMethod\": " + paymentMethodJson + "}"));
    }

    private static String card(String method, String number) {
        return """
                {"name": "%s",
                 "card": {"holderName": "Ana Souza", "number": "%s", "expiry": "12/35", "cvv": "987"}}
                """.formatted(method, number);
    }
}
