package com.larissafalcao.bff_checkout.domain.exception;

public enum ErrorCode {
    PRODUCT_NOT_FOUND,
    DUPLICATE_PRODUCT,
    CART_NOT_FOUND,
    CART_ALREADY_PAID,
    PAYMENT_IN_PROGRESS,
    INVALID_PAYMENT_METHOD,
    INVALID_PAYMENT_DATA,
    PAYMENT_DECLINED,
    SERVICE_UNAVAILABLE
}
