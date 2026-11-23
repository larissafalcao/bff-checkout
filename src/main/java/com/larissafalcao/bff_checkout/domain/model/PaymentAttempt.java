package com.larissafalcao.bff_checkout.domain.model;

import java.time.Duration;
import java.time.Instant;

public record PaymentAttempt(int number, Instant startedAt) {

    public static PaymentAttempt first() {
        return new PaymentAttempt(1, null);
    }

    public PaymentAttempt start(Instant now) {
        return new PaymentAttempt(number, now);
    }

    public PaymentAttempt release() {
        return new PaymentAttempt(number, null);
    }

    public PaymentAttempt next() {
        return new PaymentAttempt(number + 1, null);
    }

    public boolean isStale(Instant now, Duration processingTimeout) {
        return startedAt != null && startedAt.plus(processingTimeout).isBefore(now);
    }
}
