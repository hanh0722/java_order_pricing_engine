package com.ecommerce.pricing.infrastructure.persistence.adapter;

import com.ecommerce.pricing.domain.port.PromotionRulePort;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import com.ecommerce.pricing.domain.promotion.PromotionType;
import com.ecommerce.pricing.infrastructure.persistence.entity.CouponEntity;
import com.ecommerce.pricing.infrastructure.persistence.entity.PromotionEntity;
import com.ecommerce.pricing.infrastructure.persistence.repository.CouponJpaRepository;
import com.ecommerce.pricing.infrastructure.persistence.repository.PromotionJpaRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class PromotionRuleAdapter implements PromotionRulePort {

    private final PromotionJpaRepository promotionRepository;
    private final CouponJpaRepository couponRepository;

    public PromotionRuleAdapter(PromotionJpaRepository promotionRepository, CouponJpaRepository couponRepository) {
        this.promotionRepository = promotionRepository;
        this.couponRepository = couponRepository;
    }

    @Override
    @Cacheable("activePromotions")
    public List<PromotionRule> findActivePromotions() {
        return promotionRepository.findByActiveTrueOrderByPriorityAsc().stream()
                .filter(this::isWithinSchedule)
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Cacheable(value = "couponRules", key = "#couponCode")
    public Optional<PromotionRule> findCouponRule(String couponCode) {
        return couponRepository.findByCodeIgnoreCaseAndActiveTrue(couponCode)
                .filter(this::isWithinSchedule)
                .map(this::toCouponRule);
    }

    private boolean isWithinSchedule(PromotionEntity entity) {
        Instant now = Instant.now();
        if (entity.getStartsAt() != null && now.isBefore(entity.getStartsAt())) {
            return false;
        }
        if (entity.getEndsAt() != null && now.isAfter(entity.getEndsAt())) {
            return false;
        }
        return true;
    }

    private boolean isWithinSchedule(CouponEntity entity) {
        Instant now = Instant.now();
        if (entity.getStartsAt() != null && now.isBefore(entity.getStartsAt())) {
            return false;
        }
        if (entity.getEndsAt() != null && now.isAfter(entity.getEndsAt())) {
            return false;
        }
        return true;
    }

    // Builder
    private PromotionRule toDomain(PromotionEntity entity) {
        return new PromotionRule(
                entity.getCode(),
                PromotionType.valueOf(entity.getType()),
                entity.getValue(),
                entity.getBuyQuantity(),
                entity.getFreeQuantity(),
                entity.getTargetSku(),
                entity.getPriority());
    }

    private PromotionRule toCouponRule(CouponEntity coupon) {
        return new PromotionRule(
                coupon.getCode(),
                PromotionType.COUPON_FIXED,
                coupon.getDiscountAmount(),
                null,
                null,
                null,
                400);
    }
}
