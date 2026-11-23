package com.larissafalcao.bff_checkout.adapter.out.catalog;

import com.larissafalcao.bff_checkout.application.port.out.ProductCatalogPort;
import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import com.larissafalcao.bff_checkout.domain.model.Product;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class ProductCatalogHttpAdapter implements ProductCatalogPort {

    private final CatalogServiceClient client;

    public ProductCatalogHttpAdapter(CatalogServiceClient client) {
        this.client = client;
    }

    @Override
    public List<Product> findAll() {
        try {
            return client.fetchProducts().stream()
                    .map(ProductCatalogHttpAdapter::toDomain)
                    .toList();
        } catch (RestClientException exception) {
            throw new CheckoutException(ErrorCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private static Product toDomain(ProductJson json) {
        return new Product(json.slug(), json.name(), json.description(), json.price(), json.currency());
    }
}
