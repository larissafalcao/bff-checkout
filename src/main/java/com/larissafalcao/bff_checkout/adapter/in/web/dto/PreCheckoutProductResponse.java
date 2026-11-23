package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record PreCheckoutProductResponse(
        @Schema(example = "premium") String slug,
        @Schema(example = "Plano Premium") String name,
        @Schema(example = "Assinatura mensal com catálogo completo") String description,
        @Schema(example = "39.90") BigDecimal price,
        @Schema(example = "BRL") String currency) {
}
