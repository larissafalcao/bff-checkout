package com.larissafalcao.bff_checkout.adapter.out.catalog;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductJson(String slug, String name, String description, BigDecimal price, String currency)
        implements Serializable {
}
