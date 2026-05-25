package com.ecommerce.pricing.domain.promotion.strategy;

import com.ecommerce.pricing.domain.promotion.PromotionContext;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import com.ecommerce.pricing.domain.promotion.PromotionStrategy;
import com.ecommerce.pricing.domain.promotion.PromotionType;

import java.math.BigDecimal;

public class CouponFixedStrategy implements PromotionStrategy {

    @Override
    public PromotionType supportedType() {
        return PromotionType.COUPON_FIXED;
    }

    @Override
    public void apply(PromotionRule rule, PromotionContext context) {
        if (context.couponCode() == null || rule.value() == null) {
            return;
        }
        if (!context.couponCode().equalsIgnoreCase(extractCouponCode(rule))) {
            return;
        }
        context.applyDiscount(rule.code(), rule.value());
    }

    private String extractCouponCode(PromotionRule rule) {
        // Coupon rules use code field as coupon identifier (e.g. SUMMER10)
        return rule.code();
    }
}
