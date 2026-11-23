package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentMethodResponse(@Schema(example = "Cartão de Crédito") String name) {
}
