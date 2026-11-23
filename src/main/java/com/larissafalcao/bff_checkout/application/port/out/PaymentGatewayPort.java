package com.larissafalcao.bff_checkout.application.port.out;

import com.larissafalcao.bff_checkout.domain.model.ChargeResult;

public interface PaymentGatewayPort {

    /**
     * Cobranças com a mesma {@code idempotencyKey} são processadas uma única vez: repetir a chamada devolve o
     * resultado original.
     */
    ChargeResult charge(ChargeRequest request);
}
