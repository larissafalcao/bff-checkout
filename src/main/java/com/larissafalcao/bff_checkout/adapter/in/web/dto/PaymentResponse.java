package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        @Schema(example = "Cartão de Crédito") String method,
        @Schema(description = "Só em pagamentos com cartão", example = "4242") String lastDigits,
        @Schema(description = "Só em pagamentos com Pix", example = "PIX-4D8A3401") String pixCode,
        @Schema(example = "39.90") BigDecimal amount,
        @Schema(example = "BRL") String currency) {
}
