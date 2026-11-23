package com.larissafalcao.bff_checkout.mockserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/mock")
public class MockCatalogController {

    private static final List<MockProduct> PRODUCTS = List.of(
            new MockProduct("basico", "Plano Básico",
                    "Assinatura mensal com catálogo essencial", new BigDecimal("19.90"), "BRL"),
            new MockProduct("premium", "Plano Premium",
                    "Assinatura mensal com catálogo completo", new BigDecimal("39.90"), "BRL"),
            new MockProduct("familia", "Plano Família",
                    "Assinatura mensal com catálogo completo para até 4 perfis", new BigDecimal("59.90"), "BRL"));

    private static final List<MockPaymentMethod> PAYMENT_METHODS = List.of(
            new MockPaymentMethod("Cartão de Crédito", true),
            new MockPaymentMethod("Cartão de Débito", true),
            new MockPaymentMethod("Pix", false));

    @GetMapping("/products")
    public List<MockProduct> products() {
        return PRODUCTS;
    }

    @GetMapping("/payment-methods")
    public List<MockPaymentMethod> paymentMethods() {
        return PAYMENT_METHODS;
    }
}
