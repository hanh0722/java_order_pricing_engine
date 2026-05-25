package com.ecommerce.pricing.domain.promotion.strategy;

import com.ecommerce.pricing.domain.model.OrderItem;
import com.ecommerce.pricing.domain.promotion.PromotionContext;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import com.ecommerce.pricing.domain.promotion.PromotionStrategy;
import com.ecommerce.pricing.domain.promotion.PromotionType;

import java.math.BigDecimal;

public class BuyXGetYStrategy implements PromotionStrategy {

    @Override
    public PromotionType supportedType() {
        return PromotionType.BUY_X_GET_Y;
    }

    @Override
    public void apply(PromotionRule rule, PromotionContext context) {
        if (rule.buyQuantity() == null || rule.freeQuantity() == null || rule.targetSku() == null) {
            return;
        }

        int bundleSize = rule.buyQuantity() + rule.freeQuantity();
        for (OrderItem item : context.items()) {
            if (!item.sku().equals(rule.targetSku())) {
                continue;
            }
            int freeUnits = (item.quantity() / bundleSize) * rule.freeQuantity();
            if (freeUnits > 0) {
                BigDecimal discount = item.unitPrice().multiply(BigDecimal.valueOf(freeUnits));
                context.applyDiscount(rule.code(), discount);
            }
        }
    }
}
