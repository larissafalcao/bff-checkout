package com.larissafalcao.bff_checkout.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public record PaymentAttemptEmbeddable(
        @Column(name = "payment_attempt") int number,
        @Column(name = "payment_started_at") Instant startedAt) {
}
