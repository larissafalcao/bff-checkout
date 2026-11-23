package com.larissafalcao.bff_checkout.adapter.out.catalog;

import com.larissafalcao.bff_checkout.application.port.out.PaymentMethodCatalogPort;
import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import com.larissafalcao.bff_checkout.domain.model.PaymentMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class PaymentMethodCatalogHttpAdapter implements PaymentMethodCatalogPort {

    private final CatalogServiceClient client;

    public PaymentMethodCatalogHttpAdapter(CatalogServiceClient client) {
        this.client = client;
    }

    @Override
    public List<PaymentMethod> findAll() {
        try {
            return client.fetchPaymentMethods().stream()
                    .map(PaymentMethodCatalogHttpAdapter::toDomain)
                    .toList();
        } catch (RestClientException exception) {
            throw new CheckoutException(ErrorCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private static PaymentMethod toDomain(PaymentMethodJson json) {
        return new PaymentMethod(json.name(), json.requiresCard());
    }
}
