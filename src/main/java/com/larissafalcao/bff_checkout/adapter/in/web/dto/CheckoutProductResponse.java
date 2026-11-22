package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record CheckoutProductResponse(
        @Schema(example = "premium") String slug,
        @Schema(example = "Plano Premium") String name,
        @Schema(example = "39.90") BigDecimal price) {
}
