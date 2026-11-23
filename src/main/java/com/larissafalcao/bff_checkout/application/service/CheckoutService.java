package com.larissafalcao.bff_checkout.application.service;

import com.larissafalcao.bff_checkout.application.port.in.CheckoutCommand;
import com.larissafalcao.bff_checkout.application.port.in.CheckoutUseCase;
import com.larissafalcao.bff_checkout.application.port.out.CartRepositoryPort;
import com.larissafalcao.bff_checkout.application.port.out.ChargeRequest;
import com.larissafalcao.bff_checkout.application.port.out.ConcurrentCartUpdateException;
import com.larissafalcao.bff_checkout.application.port.out.PaymentGatewayPort;
import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import com.larissafalcao.bff_checkout.domain.model.Card;
import com.larissafalcao.bff_checkout.domain.model.Cart;
import com.larissafalcao.bff_checkout.domain.model.ChargeResult;
import com.larissafalcao.bff_checkout.domain.model.PaymentMethod;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;

public class CheckoutService implements CheckoutUseCase {

    private final CartRepositoryPort cartRepository;
    private final PaymentGatewayPort paymentGateway;
    private final Clock clock;
    private final Duration processingTimeout;

    public CheckoutService(CartRepositoryPort cartRepository, PaymentGatewayPort paymentGateway, Clock clock,
                           Duration processingTimeout) {
        this.cartRepository = cartRepository;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
        this.processingTimeout = processingTimeout;
    }

    @Override
    public Cart checkout(CheckoutCommand command) {
        Cart cart = cartRepository.findById(command.cartId())
                .orElseThrow(() -> new CheckoutException(ErrorCode.CART_NOT_FOUND, command.cartId()));

        cart.ensurePayable(Instant.now(clock), processingTimeout);
        PaymentMethod method = cart.offeredPaymentMethod(command.paymentMethodName());
        validatePaymentData(cart, method, command.card());

        Cart reserved = reserveForPayment(cart);
        return charge(reserved, method, command.card());
    }

    private Cart reserveForPayment(Cart cart) {
        cart.startPayment(Instant.now(clock), processingTimeout);
        return save(cart);
    }

    private Cart charge(Cart cart, PaymentMethod method, Card card) {
        ChargeResult result = requestCharge(cart, method, card);
        cart.complete(method.name(), result, Instant.now(clock));
        Cart saved = save(cart);
        if (!saved.isPaid()) {
            throw new CheckoutException(ErrorCode.PAYMENT_DECLINED, saved.getId());
        }
        return saved;
    }

    /** Outra requisição alterou o carrinho no meio do caminho: para o checkout, é um pagamento em andamento. */
    private Cart save(Cart cart) {
        try {
            return cartRepository.save(cart);
        } catch (ConcurrentCartUpdateException exception) {
            throw new CheckoutException(ErrorCode.PAYMENT_IN_PROGRESS, cart.getId(), exception);
        }
    }

    private ChargeResult requestCharge(Cart cart, PaymentMethod method, Card card) {
        ChargeRequest request = new ChargeRequest(cart.paymentIdempotencyKey(), method, card, cart.getTotal());
        try {
            return paymentGateway.charge(request);
        } catch (RuntimeException chargeFailure) {
            releaseForRetry(cart, chargeFailure);
            throw chargeFailure;
        }
    }

    private void releaseForRetry(Cart cart, RuntimeException chargeFailure) {
        try {
            cart.releaseForRetry();
            save(cart);
        } catch (RuntimeException releaseFailure) {
            chargeFailure.addSuppressed(releaseFailure);
        }
    }

    private void validatePaymentData(Cart cart, PaymentMethod method, Card card) {
        boolean valid = method.requiresCard()
                ? card != null && card.isValid(YearMonth.now(clock))
                : card == null;
        if (!valid) {
            throw new CheckoutException(ErrorCode.INVALID_PAYMENT_DATA, cart.getId());
        }
    }
}
