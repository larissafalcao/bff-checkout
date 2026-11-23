package com.larissafalcao.bff_checkout.application.port.in;

import com.larissafalcao.bff_checkout.domain.model.Cart;

public interface CheckoutUseCase {
    Cart checkout(CheckoutCommand command);
}
