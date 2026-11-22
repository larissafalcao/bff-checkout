package com.larissafalcao.bff_checkout.adapter.in.web.dto;

import com.larissafalcao.bff_checkout.application.port.in.CheckoutCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckoutRequest(
        @Schema(description = "cartId devolvido no pré-checkout", example = "5a7ed441-567e-4f63-825e-dbdf07e640d6")
        @NotNull UUID cartId,
        @NotNull @Valid PaymentMethodRequest paymentMethod) {

    public CheckoutCommand toCommand() {
        CardRequest card = paymentMethod.card();
        return new CheckoutCommand(cartId, paymentMethod.name(), card == null ? null : card.toDomain());
    }
}
