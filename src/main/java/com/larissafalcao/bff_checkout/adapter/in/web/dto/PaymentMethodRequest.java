package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PaymentMethodRequest(
        @Schema(description = "Um dos meios devolvidos no pré-checkout", example = "Cartão de Crédito")
        @NotBlank String name,
        @Schema(description = "Obrigatório para cartão; não enviar para Pix")
        CardRequest card) {
}
