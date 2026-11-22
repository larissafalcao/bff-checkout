package com.larissafalcao.bff_checkout.application.port.out;

import com.larissafalcao.bff_checkout.domain.model.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepositoryPort {

    Optional<Cart> findById(UUID id);

    /** @throws ConcurrentCartUpdateException se o carrinho mudou desde que foi lido. */
    Cart save(Cart cart);
}
