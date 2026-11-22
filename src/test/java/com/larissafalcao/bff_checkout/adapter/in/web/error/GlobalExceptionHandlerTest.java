package com.larissafalcao.bff_checkout.adapter.in.web.error;

import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    void everyErrorCodeProducesCompleteFrontendError(ErrorCode code) {
        UUID cartId = UUID.randomUUID();

        var response = handler.handleCheckoutException(new CheckoutException(code, cartId));

        assertThat(response.getStatusCode().isError()).isTrue();
        ErrorResponse body = Objects.requireNonNull(response.getBody(), "corpo da resposta");
        assertThat(body.errorCode()).isEqualTo(code.name());
        assertThat(body.cartId()).isEqualTo(cartId);
        assertThat(body.displayType()).isNotNull();
        assertThat(body.frontendError().code()).isNotBlank();
        assertThat(body.frontendError().title()).isNotBlank();
        assertThat(body.frontendError().description()).isNotBlank();
        assertThat(body.frontendError().primaryButton()).isNotNull();
    }

    @Test
    void unexpectedErrorIsInternalErrorNotServiceUnavailable() {
        var response = handler.handleUnexpectedException(new NullPointerException("bug"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ErrorResponse body = Objects.requireNonNull(response.getBody(), "corpo da resposta");
        assertThat(body.errorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.frontendError().code()).isEqualTo("P-9002");
    }
}
