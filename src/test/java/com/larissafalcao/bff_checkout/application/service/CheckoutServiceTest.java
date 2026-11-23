package com.larissafalcao.bff_checkout.application.service;

import com.larissafalcao.bff_checkout.application.port.in.CheckoutCommand;
import com.larissafalcao.bff_checkout.application.port.out.CartRepositoryPort;
import com.larissafalcao.bff_checkout.application.port.out.ChargeRequest;
import com.larissafalcao.bff_checkout.application.port.out.ConcurrentCartUpdateException;
import com.larissafalcao.bff_checkout.application.port.out.PaymentGatewayPort;
import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import com.larissafalcao.bff_checkout.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CheckoutServiceTest {

    static final Instant NOW = Instant.parse("2026-10-14T16:00:00Z");
    static final PaymentMethod CREDIT = new PaymentMethod("Cartão de Crédito", true);
    static final PaymentMethod PIX = new PaymentMethod("Pix", false);
    static final Card VALID_CARD = new Card("Ana Souza", "4111111111114242", "12/28", "123");

    @Mock CartRepositoryPort cartRepository;
    @Mock PaymentGatewayPort paymentGateway;
    CheckoutService service;
    Cart cart;

    @BeforeEach
    void setUp() {
        service = new CheckoutService(cartRepository, paymentGateway, Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofMinutes(2));
        cart = Cart.create(List.of(new Product("premium", "Plano Premium", "d", new BigDecimal("39.90"), "BRL")),
                List.of(CREDIT, new PaymentMethod("Cartão de Débito", true), PIX));
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void approvedCardPaymentKeepsOnlyLastDigits() {
        when(paymentGateway.charge(new ChargeRequest(cart.getId() + ":1", CREDIT, VALID_CARD, new BigDecimal("39.90")))).thenReturn(new CardApproved("4242"));

        Cart paid = service.checkout(new CheckoutCommand(cart.getId(), "Cartão de Crédito", VALID_CARD));

        assertThat(paid.getStatus()).isEqualTo(CartStatus.APPROVED);
        assertThat(paid.getPayment()).isEqualTo(new Payment("Cartão de Crédito", "4242", null, NOW));
        verify(cartRepository, times(2)).save(cart);
    }

    @Test
    void approvedPixPaymentHasPixCode() {
        when(paymentGateway.charge(new ChargeRequest(cart.getId() + ":1", PIX, null, new BigDecimal("39.90")))).thenReturn(new PixApproved("PIX-1"));

        Cart paid = service.checkout(new CheckoutCommand(cart.getId(), "Pix", null));

        assertThat(paid.getPayment()).isEqualTo(new Payment("Pix", null, "PIX-1", NOW));
    }

    @Test
    void declinedPaymentReleasesCartForNewAttempt() {
        when(paymentGateway.charge(any())).thenReturn(new Declined());

        assertError(new CheckoutCommand(cart.getId(), "Cartão de Crédito", VALID_CARD), ErrorCode.PAYMENT_DECLINED);
        assertThat(cart.getStatus()).isEqualTo(CartStatus.PENDING);
        assertThat(cart.paymentIdempotencyKey()).isEqualTo(cart.getId() + ":2");
    }

    @Test
    void cartIsReservedBeforeGatewayIsCalled() {
        when(paymentGateway.charge(any())).thenAnswer(invocation -> {
            assertThat(cart.getStatus()).isEqualTo(CartStatus.PROCESSING);
            return new PixApproved("PIX-1");
        });

        service.checkout(new CheckoutCommand(cart.getId(), "Pix", null));

        InOrder order = inOrder(cartRepository, paymentGateway);
        order.verify(cartRepository).save(cart);
        order.verify(paymentGateway).charge(any());
    }

    @Test
    void reservationConflictPreventsCharge() {
        when(cartRepository.save(any())).thenThrow(new ConcurrentCartUpdateException(cart.getId(), null));

        assertError(new CheckoutCommand(cart.getId(), "Pix", null), ErrorCode.PAYMENT_IN_PROGRESS);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void paymentInProgressIsRejectedWithoutCharging() {
        cart.startPayment(NOW, Duration.ofMinutes(2));

        assertError(new CheckoutCommand(cart.getId(), "Pix", null), ErrorCode.PAYMENT_IN_PROGRESS);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void gatewayFailureReleasesCartAndRetryReusesIdempotencyKey() {
        when(paymentGateway.charge(any()))
                .thenThrow(new IllegalStateException("timeout"))
                .thenReturn(new PixApproved("PIX-1"));

        assertThatThrownBy(() -> service.checkout(new CheckoutCommand(cart.getId(), "Pix", null)))
                .hasMessage("timeout");
        assertThat(cart.getStatus()).isEqualTo(CartStatus.PENDING);

        service.checkout(new CheckoutCommand(cart.getId(), "Pix", null));

        ArgumentCaptor<ChargeRequest> requests = ArgumentCaptor.forClass(ChargeRequest.class);
        verify(paymentGateway, times(2)).charge(requests.capture());
        assertThat(requests.getAllValues()).extracting(ChargeRequest::idempotencyKey)
                .containsOnly(cart.getId() + ":1");
    }

    @Test
    void unknownCartIsRejected() {
        UUID id = UUID.randomUUID();
        when(cartRepository.findById(id)).thenReturn(Optional.empty());

        assertError(new CheckoutCommand(id, "Pix", null), ErrorCode.CART_NOT_FOUND);
    }

    @Test
    void approvedCartCannotBePaidAgain() {
        when(paymentGateway.charge(any())).thenReturn(new PixApproved("PIX-1"));
        service.checkout(new CheckoutCommand(cart.getId(), "Pix", null));

        assertError(new CheckoutCommand(cart.getId(), "Pix", null), ErrorCode.CART_ALREADY_PAID);
        verify(paymentGateway, times(1)).charge(any());
    }

    @Test
    void paymentMethodMustBelongToCart() {
        assertError(new CheckoutCommand(cart.getId(), "Boleto", null), ErrorCode.INVALID_PAYMENT_METHOD);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void pixRejectsCardAndCardMethodsRequireValidCard() {
        assertError(new CheckoutCommand(cart.getId(), "Pix", VALID_CARD), ErrorCode.INVALID_PAYMENT_DATA);
        assertError(new CheckoutCommand(cart.getId(), "Cartão de Débito", null), ErrorCode.INVALID_PAYMENT_DATA);
        assertError(new CheckoutCommand(cart.getId(), "Cartão de Débito", new Card("Ana", "4111", "13/28", "1")),
                ErrorCode.INVALID_PAYMENT_DATA);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void expiredCardIsRejectedUsingInjectedClock() {
        Card expiredLastMonth = new Card("Ana Souza", "4111111111114242", "09/26", "123");

        CheckoutCommand command = new CheckoutCommand(cart.getId(), "Cartão de Crédito", expiredLastMonth);

        assertError(command, ErrorCode.INVALID_PAYMENT_DATA);
        verifyNoInteractions(paymentGateway);
    }

    private void assertError(CheckoutCommand command, ErrorCode expected) {
        assertThatThrownBy(() -> service.checkout(command)).isInstanceOfSatisfying(CheckoutException.class,
                exception -> assertThat(exception.code()).isEqualTo(expected));
    }
}
