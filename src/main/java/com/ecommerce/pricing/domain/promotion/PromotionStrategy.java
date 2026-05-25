package com.ecommerce.pricing.domain.promotion;

/**
 * Strategy pattern: each promotion type encapsulates its own discount algorithm.
 */
public interface PromotionStrategy {

    PromotionType supportedType();

    void apply(PromotionRule rule, PromotionContext context);
}
