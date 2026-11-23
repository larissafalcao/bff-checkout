package com.larissafalcao.bff_checkout.adapter.out.catalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CatalogServiceClient {

    private final RestClient products;
    private final RestClient paymentMethods;

    public CatalogServiceClient(RestClient.Builder builder,
                                @Value("${services.products.base-url}") String productsUrl,
                                @Value("${services.payment-methods.base-url}") String paymentMethodsUrl) {
        this.products = builder.clone().baseUrl(productsUrl).build();
        this.paymentMethods = builder.clone().baseUrl(paymentMethodsUrl).build();
    }

    @Cacheable("products")
    public List<ProductJson> fetchProducts() {
        return products.get().uri("/products").retrieve().body(new ParameterizedTypeReference<>() {});
    }

    @Cacheable("paymentMethods")
    public List<PaymentMethodJson> fetchPaymentMethods() {
        return paymentMethods.get().uri("/payment-methods").retrieve().body(new ParameterizedTypeReference<>() {});
    }
}
