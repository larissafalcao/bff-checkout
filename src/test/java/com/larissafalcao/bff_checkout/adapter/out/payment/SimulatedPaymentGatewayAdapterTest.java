package com.larissafalcao.bff_checkout.adapter.out.payment;

import com.larissafalcao.bff_checkout.application.port.out.ChargeRequest;
import com.larissafalcao.bff_checkout.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimulatedPaymentGatewayAdapterTest {

    final SimulatedPaymentGatewayAdapter gateway = new SimulatedPaymentGatewayAdapter();
    final PaymentMethod credit = new PaymentMethod("Cartão de Crédito", true);

    @Test
    void cardEndingIn0002IsDeclined() {
        ChargeResult result = chargeCard("4111111111110002");
        assertThat(result).isInstanceOf(Declined.class);
    }

    @Test
    void otherCardsAreApprovedWithLastDigits() {
        ChargeResult result = chargeCard("4111111111114242");
        assertThat(result).isEqualTo(new CardApproved("4242"));
    }

    @Test
    void cardEndingIn0002ButNotLastFourIsApproved() {
        ChargeResult result = chargeCard("4111000211114243");
        assertThat(result).isEqualTo(new CardApproved("4243"));
    }

    @Test
    void pixIsApprovedWithSimulatedCode() {
        ChargeResult result = gateway.charge(new ChargeRequest("cart:1", new PaymentMethod("Pix", false), null, BigDecimal.TEN));
        assertThat(result).isInstanceOfSatisfying(PixApproved.class,
                pix -> assertThat(pix.pixCode()).matches("PIX-[0-9A-F]{8}"));
    }

    @Test
    void unknownNonCardMethodIsNotTreatedAsPix() {
        assertThatThrownBy(() -> gateway.charge(new ChargeRequest("cart:1", new PaymentMethod("Boleto", false), null, BigDecimal.TEN)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ChargeResult chargeCard(String number) {
        return gateway.charge(new ChargeRequest(number, credit, new Card("Ana Souza", number, "12/35", "123"),
                BigDecimal.TEN));
    }

    @Test
    void sameIdempotencyKeyReturnsOriginalResultWithoutChargingAgain() {
        ChargeRequest pix = new ChargeRequest("cart:1", new PaymentMethod("Pix", false), null, BigDecimal.TEN);

        ChargeResult first = gateway.charge(pix);

        assertThat(gateway.charge(pix)).isSameAs(first);
        assertThat(gateway.charge(new ChargeRequest("cart:2", pix.method(), null, BigDecimal.TEN))).isNotEqualTo(first);
    }
}
