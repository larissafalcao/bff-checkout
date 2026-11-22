package com.larissafalcao.bff_checkout.application.port.out;

import com.larissafalcao.bff_checkout.domain.model.PaymentMethod;

import java.util.List;

public interface PaymentMethodCatalogPort {
    List<PaymentMethod> findAll();
}
