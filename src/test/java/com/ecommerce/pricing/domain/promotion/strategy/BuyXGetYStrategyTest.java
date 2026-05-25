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

class BuyXGetYStrategyTest {

    @Test
    void grantsOneFreeUnitWhenBuyingThree() {
        PricingResult.Builder builder = PricingResult.builder(new BigDecimal("300.00"));
        PromotionContext context = new PromotionContext(
                CustomerType.REGULAR,
                List.of(new OrderItem("A100", new BigDecimal("100"), 3)),
                null,
                new BigDecimal("300.00"),
                builder);

        PromotionRule rule = new PromotionRule(
                "BXGY_A100", PromotionType.BUY_X_GET_Y, null, 2, 1, "A100", 100);

        new BuyXGetYStrategy().apply(rule, context);

        PricingResult result = builder.build();
        assertEquals(new BigDecimal("100.00"), result.discount());
        assertEquals(new BigDecimal("200.00"), result.finalPrice());
    }

    @Test
    void doesNotApplyWhenQuantityBelowBundleSize() {
        PricingResult.Builder builder = PricingResult.builder(new BigDecimal("200.00"));
        PromotionContext context = new PromotionContext(
                CustomerType.REGULAR,
                List.of(new OrderItem("A100", new BigDecimal("100"), 2)),
                null,
                new BigDecimal("200.00"),
                builder);

        PromotionRule rule = new PromotionRule(
                "BXGY_A100", PromotionType.BUY_X_GET_Y, null, 2, 1, "A100", 100);

        new BuyXGetYStrategy().apply(rule, context);

        assertEquals(BigDecimal.ZERO, builder.build().discount());
    }
}
