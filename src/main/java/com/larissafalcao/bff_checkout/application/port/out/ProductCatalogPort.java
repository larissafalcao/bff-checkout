package com.larissafalcao.bff_checkout.application.port.out;

import com.larissafalcao.bff_checkout.domain.model.Product;

import java.util.List;

public interface ProductCatalogPort {
    List<Product> findAll();
}
