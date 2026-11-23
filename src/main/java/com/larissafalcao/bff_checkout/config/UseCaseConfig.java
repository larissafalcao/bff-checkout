package com.larissafalcao.bff_checkout.config;

import com.larissafalcao.bff_checkout.application.port.in.CheckoutUseCase;
import com.larissafalcao.bff_checkout.application.port.in.PreCheckoutUseCase;
import com.larissafalcao.bff_checkout.application.port.out.CartRepositoryPort;
import com.larissafalcao.bff_checkout.application.port.out.PaymentGatewayPort;
import com.larissafalcao.bff_checkout.application.port.out.PaymentMethodCatalogPort;
import com.larissafalcao.bff_checkout.application.port.out.ProductCatalogPort;
import com.larissafalcao.bff_checkout.application.service.CheckoutService;
import com.larissafalcao.bff_checkout.application.service.PreCheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class UseCaseConfig {

    @Bean
    PreCheckoutUseCase preCheckoutUseCase(ProductCatalogPort products, PaymentMethodCatalogPort paymentMethods,
                                          CartRepositoryPort carts) {
        return new PreCheckoutService(products, paymentMethods, carts);
    }

    @Bean
    CheckoutUseCase checkoutUseCase(CartRepositoryPort carts, PaymentGatewayPort paymentGateway,
                                    @Value("${checkout.payment.processing-timeout}") Duration processingTimeout) {
        return new CheckoutService(carts, paymentGateway, Clock.systemUTC(), processingTimeout);
    }
}
