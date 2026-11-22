package com.larissafalcao.bff_checkout.adapter.out.persistence;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public record ProductEmbeddable(String slug, String name, String description, BigDecimal price, String currency) {
}
