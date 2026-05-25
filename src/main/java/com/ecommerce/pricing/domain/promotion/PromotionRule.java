package com.ecommerce.pricing.domain.promotion;

import java.math.BigDecimal;

public record PromotionRule(
        String code,
        PromotionType type,
        BigDecimal value,
        Integer buyQuantity,
        Integer freeQuantity,
        String targetSku,
        int priority) {

        @Override
        public final String toString() {
                return this.code + " - " + this.type + " - " + this.value + " - " + this.targetSku;
        }
}
