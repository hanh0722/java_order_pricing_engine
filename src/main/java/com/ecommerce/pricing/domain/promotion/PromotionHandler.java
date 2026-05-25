package com.ecommerce.pricing.domain.promotion;

/**
 * Chain of Responsibility: handlers process promotions in priority order.
 */
public abstract class PromotionHandler {

    private PromotionHandler next;

    public PromotionHandler linkWith(PromotionHandler next) {
        this.next = next;
        return next;
    }

    public void handle(PromotionRule rule, PromotionContext context) {
        if (supports(rule)) {
            doHandle(rule, context);
        }
        if (next != null) {
            next.handle(rule, context);
        }
    }

    protected abstract boolean supports(PromotionRule rule);

    protected abstract void doHandle(PromotionRule rule, PromotionContext context);
}
