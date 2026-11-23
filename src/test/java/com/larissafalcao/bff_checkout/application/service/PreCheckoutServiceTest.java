package com.larissafalcao.bff_checkout.application.service;

import com.larissafalcao.bff_checkout.application.port.out.CartRepositoryPort;
import com.larissafalcao.bff_checkout.application.port.out.PaymentMethodCatalogPort;
import com.larissafalcao.bff_checkout.application.port.out.ProductCatalogPort;
import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import com.larissafalcao.bff_checkout.domain.model.Cart;
import com.larissafalcao.bff_checkout.domain.model.CartStatus;
import com.larissafalcao.bff_checkout.domain.model.PaymentMethod;
import com.larissafalcao.bff_checkout.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PreCheckoutServiceTest {

    @Mock ProductCatalogPort productCatalog;
    @Mock PaymentMethodCatalogPort paymentMethodCatalog;
    @Mock CartRepositoryPort cartRepository;
    @InjectMocks PreCheckoutService service;

    @BeforeEach
    void setUp() {
        when(productCatalog.findAll()).thenReturn(List.of(
                new Product("basico", "Plano Básico", "d", new BigDecimal("19.90"), "BRL"),
                new Product("premium", "Plano Premium", "d", new BigDecimal("39.90"), "BRL")));
    }

    @Test
    void buildsCartFromCatalogServices() {
        when(paymentMethodCatalog.findAll()).thenReturn(List.of(
                new PaymentMethod("Cartão de Crédito", true), new PaymentMethod("Pix", false)));
        when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cart cart = service.preCheckout(List.of("premium", "basico"));

        assertThat(cart.getStatus()).isEqualTo(CartStatus.PENDING);
        assertThat(cart.getProducts()).extracting(Product::slug).containsExactly("premium", "basico");
        assertThat(cart.getTotal()).isEqualByComparingTo("59.80");
        assertThat(cart.getPaymentMethods())
                .extracting(PaymentMethod::name)
                .containsExactly("Cartão de Crédito", "Pix");
        verify(cartRepository).save(cart);
    }

    @Test
    void rejectsUnknownProduct() {
        assertThatThrownBy(() -> service.preCheckout(List.of("premium", "ouro")))
                .isInstanceOfSatisfying(CheckoutException.class,
                        exception -> assertThat(exception.code()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateProduct() {
        assertThatThrownBy(() -> service.preCheckout(List.of("premium", "premium")))
                .isInstanceOfSatisfying(CheckoutException.class,
                        exception -> assertThat(exception.code()).isEqualTo(ErrorCode.DUPLICATE_PRODUCT));
        verify(cartRepository, never()).save(any());
    }
}
