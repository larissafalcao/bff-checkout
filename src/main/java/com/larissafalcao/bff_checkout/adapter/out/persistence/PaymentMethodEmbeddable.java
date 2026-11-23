package com.larissafalcao.bff_checkout.adapter.out.persistence;

import jakarta.persistence.Embeddable;

@Embeddable
public record PaymentMethodEmbeddable(String name, boolean requiresCard) {
}
