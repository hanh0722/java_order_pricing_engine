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

class VipCustomerStrategyTest {

    @Test
    void appliesExtraDiscountForVipCustomers() {
        PricingResult.Builder builder = PricingResult.builder(new BigDecimal("200.00"));
        PromotionContext context = new PromotionContext(
                CustomerType.VIP,
                List.of(new OrderItem("A100", new BigDecimal("100"), 2)),
                null,
                new BigDecimal("200.00"),
                builder);

        PromotionRule rule = new PromotionRule(
                "VIP5PCT", PromotionType.VIP_CUSTOMER, new BigDecimal("5"),
                null, null, null, 300);

        new VipCustomerStrategy().apply(rule, context);

        assertEquals(new BigDecimal("10.00"), builder.build().discount());
    }

    @Test
    void skipsNonVipCustomers() {
        PricingResult.Builder builder = PricingResult.builder(new BigDecimal("200.00"));
        PromotionContext context = new PromotionContext(
                CustomerType.REGULAR,
                List.of(new OrderItem("A100", new BigDecimal("100"), 2)),
                null,
                new BigDecimal("200.00"),
                builder);

        PromotionRule rule = new PromotionRule(
                "VIP5PCT", PromotionType.VIP_CUSTOMER, new BigDecimal("5"),
                null, null, null, 300);

        new VipCustomerStrategy().apply(rule, context);

        assertEquals(BigDecimal.ZERO, builder.build().discount());
    }
}
