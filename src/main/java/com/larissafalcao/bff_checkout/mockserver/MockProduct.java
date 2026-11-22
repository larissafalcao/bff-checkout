package com.larissafalcao.bff_checkout.mockserver;

import java.math.BigDecimal;

record MockProduct(String slug, String name, String description, BigDecimal price, String currency) {
}
