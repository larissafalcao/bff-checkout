package com.larissafalcao.bff_checkout.adapter.out.persistence;

import com.larissafalcao.bff_checkout.domain.model.CartStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private CartStatus status;

    private BigDecimal total;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_product", joinColumns = @JoinColumn(name = "cart_id"))
    @OrderColumn(name = "position")
    private List<ProductEmbeddable> products;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_payment_method", joinColumns = @JoinColumn(name = "cart_id"))
    @OrderColumn(name = "position")
    private List<PaymentMethodEmbeddable> paymentMethods;

    @Embedded
    private PaymentEmbeddable payment;

    @Embedded
    private PaymentAttemptEmbeddable paymentAttempt;

    @Version
    private Long version;
}
