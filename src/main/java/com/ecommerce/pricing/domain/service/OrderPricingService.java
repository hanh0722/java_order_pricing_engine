package com.ecommerce.pricing.domain.service;

import com.ecommerce.pricing.domain.model.PricingRequest;
import com.ecommerce.pricing.domain.model.PricingResult;
import com.ecommerce.pricing.domain.port.PromotionRulePort;
import com.ecommerce.pricing.domain.promotion.PromotionPipeline;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain service orchestrating pricing calculation (Single Responsibility).
 */
public class OrderPricingService {

    private final PromotionPipeline promotionPipeline;
    private final PromotionRulePort promotionRulePort;

    public OrderPricingService(PromotionPipeline promotionPipeline, PromotionRulePort promotionRulePort) {
        this.promotionPipeline = promotionPipeline;
        this.promotionRulePort = promotionRulePort;
    }

    public PricingResult calculatePrice(PricingRequest request) {
        List<PromotionRule> rules = new ArrayList<>(promotionRulePort.findActivePromotions());

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            promotionRulePort.findCouponRule(request.couponCode())
                    .ifPresent(rules::add);
        }

        // Exclude VIP when not applicable; pipeline strategies enforce eligibility
        return promotionPipeline.calculate(request, rules);
    }
}
