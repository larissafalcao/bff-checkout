package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import com.larissafalcao.bff_checkout.domain.model.Card;
import io.swagger.v3.oas.annotations.media.Schema;

public record CardRequest(
        @Schema(example = "Ana Souza") String holderName,
        @Schema(description = "13 a 19 dígitos. Final 0002 simula recusa.", example = "4111111111114242") String number,
        @Schema(description = "Validade no formato MM/AA", example = "12/35") String expiry,
        @Schema(description = "3 ou 4 dígitos. Nunca é persistido.", example = "123") String cvv) {

    public Card toDomain() {
        return new Card(holderName, number, expiry, cvv);
    }

    @Override
    public String toString() {
        return toDomain().toString();
    }
}
