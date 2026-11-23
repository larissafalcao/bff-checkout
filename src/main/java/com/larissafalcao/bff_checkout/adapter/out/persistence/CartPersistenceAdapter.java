package com.larissafalcao.bff_checkout.adapter.out.persistence;

import com.larissafalcao.bff_checkout.application.port.out.CartRepositoryPort;
import com.larissafalcao.bff_checkout.application.port.out.ConcurrentCartUpdateException;
import com.larissafalcao.bff_checkout.domain.model.Cart;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CartPersistenceAdapter implements CartRepositoryPort {

    private final CartJpaRepository repository;

    CartPersistenceAdapter(CartJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Cart> findById(UUID id) {
        return repository.findById(id).map(CartEntityMapper::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        try {
            CartEntity saved = repository.saveAndFlush(CartEntityMapper.toEntity(cart));
            return CartEntityMapper.toDomain(saved);
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ConcurrentCartUpdateException(cart.getId(), exception);
        }
    }
}
