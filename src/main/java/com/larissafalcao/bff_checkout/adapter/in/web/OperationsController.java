package com.larissafalcao.bff_checkout.adapter.in.web;

import com.larissafalcao.bff_checkout.adapter.in.web.dto.CheckoutRequest;
import com.larissafalcao.bff_checkout.adapter.in.web.dto.CheckoutResponse;
import com.larissafalcao.bff_checkout.adapter.in.web.dto.PreCheckoutRequest;
import com.larissafalcao.bff_checkout.adapter.in.web.dto.PreCheckoutResponse;
import com.larissafalcao.bff_checkout.adapter.in.web.openapi.OperationsApi;
import com.larissafalcao.bff_checkout.application.port.in.CheckoutUseCase;
import com.larissafalcao.bff_checkout.application.port.in.PreCheckoutUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operations")
public class OperationsController implements OperationsApi {

    private final PreCheckoutUseCase preCheckoutUseCase;
    private final CheckoutUseCase checkoutUseCase;

    public OperationsController(PreCheckoutUseCase preCheckoutUseCase, CheckoutUseCase checkoutUseCase) {
        this.preCheckoutUseCase = preCheckoutUseCase;
        this.checkoutUseCase = checkoutUseCase;
    }

    @Override
    @PostMapping("/pre-checkout")
    public PreCheckoutResponse preCheckout(@Valid @RequestBody PreCheckoutRequest request) {
        return PreCheckoutResponse.from(preCheckoutUseCase.preCheckout(request.slugs()));
    }

    @Override
    @PostMapping("/checkout")
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return CheckoutResponse.from(checkoutUseCase.checkout(request.toCommand()));
    }
}
