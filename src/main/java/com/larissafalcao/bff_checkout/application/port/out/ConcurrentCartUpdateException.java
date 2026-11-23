package com.larissafalcao.bff_checkout.application.port.out;

import java.util.UUID;

public class ConcurrentCartUpdateException extends RuntimeException {

    public ConcurrentCartUpdateException(UUID cartId, Throwable cause) {
        super("Carrinho " + cartId + " foi alterado por outra requisição", cause);
    }
}
