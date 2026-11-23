package com.larissafalcao.bff_checkout.application.service;

import com.larissafalcao.bff_checkout.application.port.in.PreCheckoutUseCase;
import com.larissafalcao.bff_checkout.application.port.out.CartRepositoryPort;
import com.larissafalcao.bff_checkout.application.port.out.PaymentMethodCatalogPort;
import com.larissafalcao.bff_checkout.application.port.out.ProductCatalogPort;
import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;
import com.larissafalcao.bff_checkout.domain.model.Cart;
import com.larissafalcao.bff_checkout.domain.model.Product;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PreCheckoutService implements PreCheckoutUseCase {

    private final ProductCatalogPort productCatalog;
    private final PaymentMethodCatalogPort paymentMethodCatalog;
    private final CartRepositoryPort cartRepository;

    public PreCheckoutService(ProductCatalogPort productCatalog, PaymentMethodCatalogPort paymentMethodCatalog,
                              CartRepositoryPort cartRepository) {
        this.productCatalog = productCatalog;
        this.paymentMethodCatalog = paymentMethodCatalog;
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart preCheckout(List<String> productSlugs) {
        ensureNoDuplicates(productSlugs);
        List<Product> products = findProducts(productSlugs);
        return cartRepository.save(Cart.create(products, paymentMethodCatalog.findAll()));
    }

    private static void ensureNoDuplicates(List<String> productSlugs) {
        if (new HashSet<>(productSlugs).size() != productSlugs.size()) {
            throw new CheckoutException(ErrorCode.DUPLICATE_PRODUCT);
        }
    }

    private List<Product> findProducts(List<String> productSlugs) {
        Map<String, Product> catalogBySlug = productCatalog.findAll().stream()
                .collect(Collectors.toMap(Product::slug, Function.identity()));
        return productSlugs.stream()
                .map(slug -> findProduct(catalogBySlug, slug))
                .toList();
    }

    private static Product findProduct(Map<String, Product> catalogBySlug, String slug) {
        Product product = catalogBySlug.get(slug);
        if (product == null) {
            throw new CheckoutException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }
}
