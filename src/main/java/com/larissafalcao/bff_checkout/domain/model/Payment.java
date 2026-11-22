package com.larissafalcao.bff_checkout.domain.model;

import java.time.Instant;

public record Payment(String method, String lastDigits, String pixCode, Instant paidAt) {
}
