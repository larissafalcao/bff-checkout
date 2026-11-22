package com.larissafalcao.bff_checkout.domain.model;

import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    static final Instant NOW = Instant.parse("2026-10-14T16:00:00Z");
    static final Duration TIMEOUT = Duration.ofMinutes(2);

    final Cart cart = Cart.create(
            List.of(new Product("premium", "Plano Premium", "d", new BigDecimal("39.90"), "BRL")),
            List.of(new PaymentMethod("Pix", false)));

    @Test
    void approvedChargeClosesCartWithPayment() {
        cart.startPayment(NOW, TIMEOUT);
        cart.complete("Pix", new PixApproved("PIX-1"), NOW);

        assertThat(cart.isPaid()).isTrue();
        assertThat(cart.getPayment()).isEqualTo(new Payment("Pix", null, "PIX-1", NOW));
    }

    @Test
    void cartMustBeReservedBeforeCompletion() {
        assertThatThrownBy(() -> cart.complete("Pix", new PixApproved("PIX-1"), NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void approvedCartCannotStartAnotherPayment() {
        cart.startPayment(NOW, TIMEOUT);
        cart.complete("Pix", new PixApproved("PIX-1"), NOW);

        assertErrorCode(() -> cart.startPayment(NOW, TIMEOUT), ErrorCode.CART_ALREADY_PAID);
        assertThat(cart.getPayment().pixCode()).isEqualTo("PIX-1");
    }

    @Test
    void paymentInProgressBlocksAnotherAttemptUntilItGoesStale() {
        cart.startPayment(NOW, TIMEOUT);

        assertErrorCode(() -> cart.startPayment(NOW.plus(TIMEOUT), TIMEOUT), ErrorCode.PAYMENT_IN_PROGRESS);

        cart.startPayment(NOW.plus(TIMEOUT).plusSeconds(1), TIMEOUT);
        assertThat(cart.paymentIdempotencyKey()).isEqualTo(cart.getId() + ":1");
    }

    @Test
    void declineOpensNewAttemptWithNewIdempotencyKey() {
        cart.startPayment(NOW, TIMEOUT);
        cart.complete("Pix", new Declined(), NOW);

        assertThat(cart.getStatus()).isEqualTo(CartStatus.PENDING);
        assertThat(cart.getPayment()).isNull();
        assertThat(cart.paymentIdempotencyKey()).isEqualTo(cart.getId() + ":2");
    }

    @Test
    void technicalFailureReleasesCartKeepingIdempotencyKey() {
        cart.startPayment(NOW, TIMEOUT);
        cart.releaseForRetry();

        assertThat(cart.getStatus()).isEqualTo(CartStatus.PENDING);
        assertThat(cart.getPaymentAttempt()).isEqualTo(new PaymentAttempt(1, null));
    }

    @Test
    void currencyComesFromProducts() {
        assertThat(cart.getCurrency()).isEqualTo("BRL");
    }

    private static void assertErrorCode(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(CheckoutException.class,
                exception -> assertThat(exception.code()).isEqualTo(expected));
    }
}
