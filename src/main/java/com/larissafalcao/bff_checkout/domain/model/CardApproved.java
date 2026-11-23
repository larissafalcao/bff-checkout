package com.larissafalcao.bff_checkout.domain.model;

public record CardApproved(String lastDigits) implements ChargeResult {
}
