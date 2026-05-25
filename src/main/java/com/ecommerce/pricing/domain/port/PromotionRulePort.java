package com.ecommerce.pricing.domain.port;

import com.ecommerce.pricing.domain.promotion.PromotionRule;

import java.util.List;
import java.util.Optional;

public interface PromotionRulePort {

    List<PromotionRule> findActivePromotions();

    Optional<PromotionRule> findCouponRule(String couponCode);
}
