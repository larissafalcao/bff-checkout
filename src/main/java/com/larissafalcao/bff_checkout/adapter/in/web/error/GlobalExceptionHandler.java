package com.larissafalcao.bff_checkout.adapter.in.web.error;

import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CheckoutException.class)
    ResponseEntity<ErrorResponse> handleCheckoutException(CheckoutException exception) {
        if (exception.getCause() != null) {
            log.error("Falha no checkout: {}", exception.code(), exception.getCause());
        }
        FrontendErrorCatalog error = FrontendErrorCatalog.of(exception.code());
        return respond(error, exception.cartId(), error.status);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErrorResponse> handleInvalidRequest(Exception exception) {
        return respond(FrontendErrorCatalog.INVALID_REQUEST, null, FrontendErrorCatalog.INVALID_REQUEST.status);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        if (exception instanceof org.springframework.web.ErrorResponse frameworkError) {
            return respond(FrontendErrorCatalog.INVALID_REQUEST, null, frameworkError.getStatusCode());
        }
        log.error("Erro inesperado", exception);
        return respond(FrontendErrorCatalog.INTERNAL_ERROR, null, FrontendErrorCatalog.INTERNAL_ERROR.status);
    }

    private static ResponseEntity<ErrorResponse> respond(FrontendErrorCatalog error, UUID cartId,
                                                         HttpStatusCode status) {
        return ResponseEntity.status(status).body(ErrorResponse.of(error, cartId));
    }
}
