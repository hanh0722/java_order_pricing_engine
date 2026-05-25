package com.ecommerce.pricing.infrastructure.persistence.adapter;

import com.ecommerce.pricing.domain.exception.CouponExhaustedException;
import com.ecommerce.pricing.domain.exception.CouponNotFoundException;
import com.ecommerce.pricing.infrastructure.cache.PromotionCacheEvictionService;
import com.ecommerce.pricing.infrastructure.persistence.entity.CouponEntity;
import com.ecommerce.pricing.infrastructure.persistence.repository.CouponJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponUsageAdapterTest {

    @Mock
    private CouponJpaRepository couponRepository;

    @Mock
    private PromotionCacheEvictionService cacheEvictionService;

    @InjectMocks
    private CouponUsageAdapter couponUsageAdapter;

    @Test
    void reserveIncrementsUsedCountWhenAvailable() {
        CouponEntity coupon = new CouponEntity();
        coupon.setCode("LIMITED1");
        coupon.setDiscountAmount(new BigDecimal("5.00"));
        coupon.setActive(true);
        coupon.setMaxUses(5);
        coupon.setUsedCount(0);

        when(couponRepository.findByCodeForUpdate("LIMITED1")).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        couponUsageAdapter.reserve("LIMITED1");

        assertEquals(1, coupon.getUsedCount());
        verify(cacheEvictionService).evictCoupon("LIMITED1");
    }

    @Test
    void reserveThrowsWhenCouponExhausted() {
        CouponEntity coupon = new CouponEntity();
        coupon.setCode("LIMITED1");
        coupon.setDiscountAmount(new BigDecimal("5.00"));
        coupon.setActive(true);
        coupon.setMaxUses(1);
        coupon.setUsedCount(1);

        when(couponRepository.findByCodeForUpdate("LIMITED1")).thenReturn(Optional.of(coupon));

        assertThrows(CouponExhaustedException.class, () -> couponUsageAdapter.reserve("LIMITED1"));
    }

    @Test
    void reserveThrowsWhenCouponNotFound() {
        when(couponRepository.findByCodeForUpdate("MISSING")).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> couponUsageAdapter.reserve("MISSING"));
    }
}
