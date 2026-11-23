package com.larissafalcao.bff_checkout.application.port.in;

import com.larissafalcao.bff_checkout.domain.model.Card;

import java.util.UUID;

public record CheckoutCommand(UUID cartId, String paymentMethodName, Card card) {
}
