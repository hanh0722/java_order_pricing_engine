package com.ecommerce.pricing.domain.promotion.chain;

import com.ecommerce.pricing.domain.promotion.PromotionContext;
import com.ecommerce.pricing.domain.promotion.PromotionHandler;
import com.ecommerce.pricing.domain.promotion.PromotionPipeline;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import com.ecommerce.pricing.domain.promotion.PromotionType;

public class CouponPromotionHandler extends PromotionHandler {

    private final PromotionPipeline pipeline;

    public CouponPromotionHandler(PromotionPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    protected boolean supports(PromotionRule rule) {
        return rule.type() == PromotionType.COUPON_FIXED;
    }

    @Override
    protected void doHandle(PromotionRule rule, PromotionContext context) {
        pipeline.applyRule(rule, context);
    }
}
