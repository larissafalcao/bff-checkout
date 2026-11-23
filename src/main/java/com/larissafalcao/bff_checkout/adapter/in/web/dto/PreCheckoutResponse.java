package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import com.larissafalcao.bff_checkout.domain.model.Cart;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record PreCheckoutResponse(
        @Schema(description = "Identificador do carrinho, usado no checkout",
                example = "5a7ed441-567e-4f63-825e-dbdf07e640d6")
        UUID cartId,
        List<PreCheckoutProductResponse> products,
        @Schema(description = "Meios de pagamento aceitos para este carrinho")
        List<PaymentMethodResponse> paymentMethods) {

    public static PreCheckoutResponse from(Cart cart) {
        List<PreCheckoutProductResponse> products = cart.getProducts().stream()
                .map(product -> new PreCheckoutProductResponse(product.slug(), product.name(), product.description(),
                        product.price(), product.currency()))
                .toList();
        List<PaymentMethodResponse> paymentMethods = cart.getPaymentMethods().stream()
                .map(paymentMethod -> new PaymentMethodResponse(paymentMethod.name()))
                .toList();
        return new PreCheckoutResponse(cart.getId(), products, paymentMethods);
    }
}
