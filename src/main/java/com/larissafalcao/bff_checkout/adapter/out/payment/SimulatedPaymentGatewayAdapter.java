package com.larissafalcao.bff_checkout.adapter.out.payment;

import com.larissafalcao.bff_checkout.application.port.out.ChargeRequest;
import com.larissafalcao.bff_checkout.application.port.out.PaymentGatewayPort;
import com.larissafalcao.bff_checkout.domain.model.CardApproved;
import com.larissafalcao.bff_checkout.domain.model.ChargeResult;
import com.larissafalcao.bff_checkout.domain.model.Declined;
import com.larissafalcao.bff_checkout.domain.model.PixApproved;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulatedPaymentGatewayAdapter implements PaymentGatewayPort {

    static final String DECLINED_CARD_SUFFIX = "0002";
    static final String PIX = "Pix";
    static final String PIX_CODE_PREFIX = "PIX-";
    static final int PIX_CODE_LENGTH = 8;

    // resultados em memória e sem expiração porque é um mock
    private final Map<String, ChargeResult> processedCharges = new ConcurrentHashMap<>();

    @Override
    public ChargeResult charge(ChargeRequest request) {
        return processedCharges.computeIfAbsent(request.idempotencyKey(), key -> process(request));
    }

    private static ChargeResult process(ChargeRequest request) {
        if (request.method().requiresCard()) {
            boolean declined = request.card().number().endsWith(DECLINED_CARD_SUFFIX);
            return declined ? new Declined() : new CardApproved(request.card().lastDigits());
        }
        if (PIX.equals(request.method().name())) {
            return new PixApproved(generatePixCode());
        }
        throw new IllegalArgumentException("Meio de pagamento não suportado pelo gateway: " + request.method().name());
    }

    private static String generatePixCode() {
        String randomPart = UUID.randomUUID().toString().substring(0, PIX_CODE_LENGTH).toUpperCase();
        return PIX_CODE_PREFIX + randomPart;
    }
}
