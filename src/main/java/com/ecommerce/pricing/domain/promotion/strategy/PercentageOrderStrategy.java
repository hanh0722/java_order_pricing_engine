package com.ecommerce.pricing.domain.promotion.strategy;

import com.ecommerce.pricing.domain.promotion.PromotionContext;
import com.ecommerce.pricing.domain.promotion.PromotionPipeline;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import com.ecommerce.pricing.domain.promotion.PromotionStrategy;
import com.ecommerce.pricing.domain.promotion.PromotionType;

import java.math.BigDecimal;

public class PercentageOrderStrategy implements PromotionStrategy {

    @Override
    public PromotionType supportedType() {
        return PromotionType.PERCENTAGE_ORDER;
    }

    @Override
    public void apply(PromotionRule rule, PromotionContext context) {
        if (rule.value() == null) {
            return;
        }
        BigDecimal discount = PromotionPipeline.percentageOf(context.runningTotal(), rule.value());
        context.applyDiscount(rule.code(), discount);
    }
}
