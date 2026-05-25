package com.ecommerce.pricing.domain.promotion.strategy;

import com.ecommerce.pricing.domain.model.CustomerType;
import com.ecommerce.pricing.domain.model.OrderItem;
import com.ecommerce.pricing.domain.model.PricingResult;
import com.ecommerce.pricing.domain.promotion.PromotionContext;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import com.ecommerce.pricing.domain.promotion.PromotionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CouponFixedStrategyTest {

    @Test
    void appliesFixedDiscountWhenCouponMatches() {
        PricingResult.Builder builder = PricingResult.builder(new BigDecimal("250.00"));
        PromotionContext context = new PromotionContext(
                CustomerType.VIP,
                List.of(new OrderItem("A100", new BigDecimal("100"), 2)),
                "SUMMER10",
                new BigDecimal("250.00"),
                builder);

        PromotionRule rule = new PromotionRule(
                "SUMMER10", PromotionType.COUPON_FIXED, new BigDecimal("10"),
                null, null, null, 400);

        new CouponFixedStrategy().apply(rule, context);

        assertEquals(new BigDecimal("10.00"), builder.build().discount());
    }
}
