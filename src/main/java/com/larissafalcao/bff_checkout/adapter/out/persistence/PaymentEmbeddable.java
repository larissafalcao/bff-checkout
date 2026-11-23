package com.larissafalcao.bff_checkout.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public record PaymentEmbeddable(
        @Column(name = "payment_method") String method,
        String lastDigits,
        String pixCode,
        Instant paidAt) {
}
