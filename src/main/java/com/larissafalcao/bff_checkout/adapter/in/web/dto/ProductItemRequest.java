package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ProductItemRequest(
        @Schema(description = "Identificador do plano no catálogo", example = "premium",
                allowableValues = {"basico", "premium", "familia"})
        @NotBlank String slug) {
}
