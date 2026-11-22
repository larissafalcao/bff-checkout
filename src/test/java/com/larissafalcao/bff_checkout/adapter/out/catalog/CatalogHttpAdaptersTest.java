package com.larissafalcao.bff_checkout.adapter.out.catalog;

import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import com.larissafalcao.bff_checkout.domain.model.PaymentMethod;
import com.larissafalcao.bff_checkout.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatalogHttpAdaptersTest {

    MockRestServiceServer server;
    ProductCatalogHttpAdapter products;
    PaymentMethodCatalogHttpAdapter paymentMethods;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        CatalogServiceClient client = new CatalogServiceClient(builder, "http://products", "http://payments");
        products = new ProductCatalogHttpAdapter(client);
        paymentMethods = new PaymentMethodCatalogHttpAdapter(client);
    }

    @Test
    void mapsProductsFromService() {
        server.expect(requestTo("http://products/products")).andRespond(withSuccess("""
                [{"slug":"premium","name":"Plano Premium","description":"d","price":39.90,"currency":"BRL"}]
                """, MediaType.APPLICATION_JSON));

        assertThat(products.findAll())
                .containsExactly(new Product("premium", "Plano Premium", "d", new BigDecimal("39.90"), "BRL"));
        server.verify();
    }

    @Test
    void mapsPaymentMethodsFromService() {
        server.expect(requestTo("http://payments/payment-methods")).andRespond(withSuccess("""
                [{"name":"Cartão de Crédito","requiresCard":true},{"name":"Pix","requiresCard":false}]
                """, MediaType.APPLICATION_JSON));

        assertThat(paymentMethods.findAll())
                .containsExactly(new PaymentMethod("Cartão de Crédito", true), new PaymentMethod("Pix", false));
        server.verify();
    }

    @Test
    void productServiceFailureBecomesServiceUnavailable() {
        server.expect(requestTo("http://products/products")).andRespond(withServerError());

        assertThatThrownBy(products::findAll).isInstanceOfSatisfying(CheckoutException.class,
                exception -> assertThat(exception.code()).isEqualTo(ErrorCode.SERVICE_UNAVAILABLE));
    }

    @Test
    void paymentMethodServiceFailureBecomesServiceUnavailable() {
        server.expect(requestTo("http://payments/payment-methods")).andRespond(withServerError());

        assertThatThrownBy(paymentMethods::findAll).isInstanceOfSatisfying(CheckoutException.class,
                exception -> assertThat(exception.code()).isEqualTo(ErrorCode.SERVICE_UNAVAILABLE));
    }
}
