package com.ecommerce.pricing.infrastructure.persistence.repository;

import com.ecommerce.pricing.infrastructure.persistence.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionJpaRepository extends JpaRepository<PromotionEntity, Long> {

    List<PromotionEntity> findByActiveTrueOrderByPriorityAsc();
}
