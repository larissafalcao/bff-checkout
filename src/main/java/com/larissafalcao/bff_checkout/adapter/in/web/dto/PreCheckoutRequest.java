package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PreCheckoutRequest(
        @Schema(description = "Planos escolhidos (sem repetição)")
        @NotEmpty List<@NotNull @Valid ProductItemRequest> products) {

    public List<String> slugs() {
        return products.stream().map(ProductItemRequest::slug).toList();
    }
}
