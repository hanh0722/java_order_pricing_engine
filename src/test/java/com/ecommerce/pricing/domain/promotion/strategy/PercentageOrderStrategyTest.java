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

class PercentageOrderStrategyTest {

    @Test
    void appliesPercentageToRunningTotal() {
        PricingResult.Builder builder = PricingResult.builder(new BigDecimal("250.00"));
        PromotionContext context = new PromotionContext(
                CustomerType.REGULAR,
                List.of(new OrderItem("A100", new BigDecimal("100"), 2)),
                null,
                new BigDecimal("250.00"),
                builder);

        PromotionRule rule = new PromotionRule(
                "ORDER10PCT", PromotionType.PERCENTAGE_ORDER, new BigDecimal("10"),
                null, null, null, 200);

        new PercentageOrderStrategy().apply(rule, context);

        PricingResult result = builder.build();
        assertEquals(new BigDecimal("25.00"), result.discount());
        assertEquals(new BigDecimal("225.00"), result.finalPrice());
    }
}
