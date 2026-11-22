package com.larissafalcao.bff_checkout.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CartJpaRepository extends JpaRepository<CartEntity, UUID> {
}
