package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import com.larissafalcao.bff_checkout.domain.model.Cart;
import com.larissafalcao.bff_checkout.domain.model.CartStatus;
import com.larissafalcao.bff_checkout.domain.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CheckoutResponse(
        @Schema(example = "5a7ed441-567e-4f63-825e-dbdf07e640d6") UUID cartId,
        @Schema(example = "APROVADO") String status,
        @Schema(example = "2026-10-14T16:00:00Z") Instant paidAt,
        PaymentResponse payment,
        List<CheckoutProductResponse> products) {

    public static CheckoutResponse from(Cart cart) {
        Payment payment = cart.getPayment();
        PaymentResponse paymentResponse = new PaymentResponse(payment.method(), payment.lastDigits(),
                payment.pixCode(), cart.getTotal(), cart.getCurrency());
        List<CheckoutProductResponse> products = cart.getProducts().stream()
                .map(product -> new CheckoutProductResponse(product.slug(), product.name(), product.price()))
                .toList();
        return new CheckoutResponse(cart.getId(), statusLabel(cart.getStatus()), payment.paidAt(), paymentResponse,
                products);
    }

    private static String statusLabel(CartStatus status) {
        return switch (status) {
            case PENDING -> "PENDENTE";
            case PROCESSING -> "PROCESSANDO";
            case APPROVED -> "APROVADO";
        };
    }
}
