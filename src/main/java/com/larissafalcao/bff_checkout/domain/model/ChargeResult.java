package com.larissafalcao.bff_checkout.domain.model;

public sealed interface ChargeResult permits CardApproved, PixApproved, Declined {
}
