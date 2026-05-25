package com.ecommerce.pricing.infrastructure.persistence.adapter;

import com.ecommerce.pricing.domain.exception.CouponExhaustedException;
import com.ecommerce.pricing.domain.exception.CouponNotFoundException;
import com.ecommerce.pricing.domain.port.CouponUsagePort;
import com.ecommerce.pricing.infrastructure.cache.PromotionCacheEvictionService;
import com.ecommerce.pricing.infrastructure.persistence.entity.CouponEntity;
import com.ecommerce.pricing.infrastructure.persistence.repository.CouponJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class CouponUsageAdapter implements CouponUsagePort {

    private final CouponJpaRepository couponRepository;
    private final PromotionCacheEvictionService cacheEvictionService;

    public CouponUsageAdapter(
            CouponJpaRepository couponRepository,
            PromotionCacheEvictionService cacheEvictionService) {
        this.couponRepository = couponRepository;
        this.cacheEvictionService = cacheEvictionService;
    }

    @Override
    @Transactional
    public void reserve(String couponCode) {
        // Lock here
        CouponEntity coupon = couponRepository.findByCodeForUpdate(couponCode)
                .filter(this::isWithinSchedule)
                .orElseThrow(() -> new CouponNotFoundException(couponCode));

        if (!coupon.hasRemainingUses()) {
            throw new CouponExhaustedException(couponCode);
        }

        coupon.incrementUsedCount();
        couponRepository.save(coupon);
        cacheEvictionService.evictCoupon(couponCode);
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
}
