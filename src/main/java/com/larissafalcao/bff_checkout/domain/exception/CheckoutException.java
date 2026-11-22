package com.larissafalcao.bff_checkout.domain.exception;

import java.util.UUID;

public class CheckoutException extends RuntimeException {

    private final ErrorCode code;
    private final UUID cartId;

    public CheckoutException(ErrorCode code) {
        this(code, null, null);
    }

    public CheckoutException(ErrorCode code, Throwable cause) {
        this(code, null, cause);
    }

    public CheckoutException(ErrorCode code, UUID cartId) {
        this(code, cartId, null);
    }

    public CheckoutException(ErrorCode code, UUID cartId, Throwable cause) {
        super(code.name(), cause);
        this.code = code;
        this.cartId = cartId;
    }

    public ErrorCode code() {
        return code;
    }

    public UUID cartId() {
        return cartId;
    }
}
