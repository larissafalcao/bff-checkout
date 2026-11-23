package com.larissafalcao.bff_checkout.application.port.in;

import com.larissafalcao.bff_checkout.domain.model.Cart;

import java.util.List;

public interface PreCheckoutUseCase {
    Cart preCheckout(List<String> productSlugs);
}
