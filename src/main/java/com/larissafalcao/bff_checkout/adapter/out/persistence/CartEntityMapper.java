package com.larissafalcao.bff_checkout.adapter.out.persistence;

import com.larissafalcao.bff_checkout.domain.model.Cart;
import com.larissafalcao.bff_checkout.domain.model.Payment;
import com.larissafalcao.bff_checkout.domain.model.PaymentAttempt;
import com.larissafalcao.bff_checkout.domain.model.PaymentMethod;
import com.larissafalcao.bff_checkout.domain.model.Product;

final class CartEntityMapper {

    private CartEntityMapper() {
    }

    static CartEntity toEntity(Cart cart) {
        return new CartEntity(
                cart.getId(),
                cart.getStatus(),
                cart.getTotal(),
                cart.getProducts().stream().map(CartEntityMapper::toEmbeddable).toList(),
                cart.getPaymentMethods().stream().map(CartEntityMapper::toEmbeddable).toList(),
                toEmbeddable(cart.getPayment()),
                toEmbeddable(cart.getPaymentAttempt()),
                cart.getVersion());
    }

    static Cart toDomain(CartEntity entity) {
        return new Cart(
                entity.getId(),
                entity.getStatus(),
                entity.getTotal(),
                entity.getProducts().stream().map(CartEntityMapper::toDomain).toList(),
                entity.getPaymentMethods().stream().map(CartEntityMapper::toDomain).toList(),
                toDomain(entity.getPayment()),
                toDomain(entity.getPaymentAttempt()),
                entity.getVersion());
    }

    private static ProductEmbeddable toEmbeddable(Product product) {
        return new ProductEmbeddable(product.slug(), product.name(), product.description(), product.price(),
                product.currency());
    }

    private static PaymentMethodEmbeddable toEmbeddable(PaymentMethod paymentMethod) {
        return new PaymentMethodEmbeddable(paymentMethod.name(), paymentMethod.requiresCard());
    }

    private static PaymentEmbeddable toEmbeddable(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentEmbeddable(payment.method(), payment.lastDigits(), payment.pixCode(), payment.paidAt());
    }

    private static PaymentAttemptEmbeddable toEmbeddable(PaymentAttempt attempt) {
        return new PaymentAttemptEmbeddable(attempt.number(), attempt.startedAt());
    }

    private static PaymentAttempt toDomain(PaymentAttemptEmbeddable attempt) {
        return new PaymentAttempt(attempt.number(), attempt.startedAt());
    }

    private static Product toDomain(ProductEmbeddable product) {
        return new Product(product.slug(), product.name(), product.description(), product.price(), product.currency());
    }

    private static PaymentMethod toDomain(PaymentMethodEmbeddable paymentMethod) {
        return new PaymentMethod(paymentMethod.name(), paymentMethod.requiresCard());
    }

    private static Payment toDomain(PaymentEmbeddable payment) {
        if (payment == null) {
            return null;
        }
        return new Payment(payment.method(), payment.lastDigits(), payment.pixCode(), payment.paidAt());
    }
}
