package com.ecommerce.pricing.infrastructure.persistence.repository;

import com.ecommerce.pricing.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {}
