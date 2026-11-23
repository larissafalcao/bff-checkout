package com.larissafalcao.bff_checkout.adapter.out.catalog;

import java.io.Serializable;

public record PaymentMethodJson(String name, boolean requiresCard) implements Serializable {
}
