package com.larissafalcao.bff_checkout.domain.model;

import com.larissafalcao.bff_checkout.domain.exception.CheckoutException;
import com.larissafalcao.bff_checkout.domain.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Cart {

    private final UUID id;
    private CartStatus status;
    private final BigDecimal total;
    private final List<Product> products;
    private final List<PaymentMethod> paymentMethods;
    private Payment payment;
    private PaymentAttempt paymentAttempt;
    private final Long version;

    public Cart(UUID id, CartStatus status, BigDecimal total, List<Product> products,
                List<PaymentMethod> paymentMethods, Payment payment, PaymentAttempt paymentAttempt, Long version) {
        this.id = id;
        this.status = status;
        this.total = total;
        this.products = List.copyOf(products);
        this.paymentMethods = List.copyOf(paymentMethods);
        this.payment = payment;
        this.paymentAttempt = paymentAttempt;
        this.version = version;
    }

    public static Cart create(List<Product> products, List<PaymentMethod> paymentMethods) {
        BigDecimal total = products.stream().map(Product::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Cart(UUID.randomUUID(), CartStatus.PENDING, total, products, paymentMethods, null,
                PaymentAttempt.first(), null);
    }

    public PaymentMethod offeredPaymentMethod(String name) {
        return paymentMethods.stream()
                .filter(paymentMethod -> paymentMethod.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new CheckoutException(ErrorCode.INVALID_PAYMENT_METHOD, id));
    }

    public void ensurePayable(Instant now, Duration processingTimeout) {
        if (isPaid()) {
            throw new CheckoutException(ErrorCode.CART_ALREADY_PAID, id);
        }
        if (status == CartStatus.PROCESSING && !paymentAttempt.isStale(now, processingTimeout)) {
            throw new CheckoutException(ErrorCode.PAYMENT_IN_PROGRESS, id);
        }
    }

    public void startPayment(Instant now, Duration processingTimeout) {
        ensurePayable(now, processingTimeout);
        this.status = CartStatus.PROCESSING;
        this.paymentAttempt = paymentAttempt.start(now);
    }

    public String paymentIdempotencyKey() {
        return id + ":" + paymentAttempt.number();
    }

    public void complete(String paymentMethodName, ChargeResult charge, Instant chargedAt) {
        ensureProcessing();
        switch (charge) {
            case CardApproved(String lastDigits) -> approve(new Payment(paymentMethodName, lastDigits, null, chargedAt));
            case PixApproved(String pixCode) -> approve(new Payment(paymentMethodName, null, pixCode, chargedAt));
            case Declined() -> reopen(paymentAttempt.next());
        }
    }

    /** Falha técnica na cobrança: libera o carrinho mantendo a tentativa (e a chave de idempotência). */
    public void releaseForRetry() {
        ensureProcessing();
        reopen(paymentAttempt.release());
    }

    public boolean isPaid() {
        return status == CartStatus.APPROVED;
    }

    public String getCurrency() {
        return products.isEmpty() ? null : products.getFirst().currency();
    }

    private void approve(Payment approvedPayment) {
        this.payment = approvedPayment;
        this.status = CartStatus.APPROVED;
    }

    private void reopen(PaymentAttempt attempt) {
        this.status = CartStatus.PENDING;
        this.paymentAttempt = attempt;
    }

    private void ensureProcessing() {
        if (status != CartStatus.PROCESSING) {
            throw new IllegalStateException("Carrinho " + id + " não está em processamento: " + status);
        }
    }

    public UUID getId() { return id; }
    public CartStatus getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public List<Product> getProducts() { return products; }
    public List<PaymentMethod> getPaymentMethods() { return paymentMethods; }
    public Payment getPayment() { return payment; }
    public PaymentAttempt getPaymentAttempt() { return paymentAttempt; }
    public Long getVersion() { return version; }
}
