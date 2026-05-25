package com.ecommerce.pricing.domain.promotion.chain;

import com.ecommerce.pricing.domain.promotion.PromotionContext;
import com.ecommerce.pricing.domain.promotion.PromotionHandler;
import com.ecommerce.pricing.domain.promotion.PromotionPipeline;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import com.ecommerce.pricing.domain.promotion.PromotionType;

public class ItemLevelPromotionHandler extends PromotionHandler {

    private final PromotionPipeline pipeline;

    public ItemLevelPromotionHandler(PromotionPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    protected boolean supports(PromotionRule rule) {
        return rule.type() == PromotionType.BUY_X_GET_Y;
    }

    @Override
    protected void doHandle(PromotionRule rule, PromotionContext context) {
        pipeline.applyRule(rule, context);
    }
}
