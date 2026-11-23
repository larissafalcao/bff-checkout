package com.larissafalcao.bff_checkout.application.port.out;

import com.larissafalcao.bff_checkout.domain.model.Card;
import com.larissafalcao.bff_checkout.domain.model.PaymentMethod;

import java.math.BigDecimal;

public record ChargeRequest(String idempotencyKey, PaymentMethod method, Card card, BigDecimal amount) {
}
