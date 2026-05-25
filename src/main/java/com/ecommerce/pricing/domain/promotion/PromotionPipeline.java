package com.ecommerce.pricing.domain.promotion;

import com.ecommerce.pricing.domain.model.CustomerType;
import com.ecommerce.pricing.domain.model.OrderItem;
import com.ecommerce.pricing.domain.model.PricingRequest;
import com.ecommerce.pricing.domain.model.PricingResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PromotionPipeline {

    private final Map<PromotionType, PromotionStrategy> strategies;
    private PromotionHandler chainHead;

    public PromotionPipeline(List<PromotionStrategy> strategyList) {
        this.strategies = new EnumMap<>(PromotionType.class);
        for (PromotionStrategy strategy : strategyList) {
            this.strategies.put(strategy.supportedType(), strategy);
        }
    }

    public void setChainHead(PromotionHandler chainHead) {
        this.chainHead = chainHead;
    }

    public PricingResult calculate(PricingRequest request, List<PromotionRule> rules) {
        BigDecimal subtotal = request.items().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PricingResult.Builder builder = PricingResult.builder(subtotal);
        PromotionContext context = new PromotionContext(
                request.customerType(),
                request.items(),
                request.couponCode(),
                subtotal,
                builder);

        List<PromotionRule> sortedRules = rules.stream()
                .sorted(Comparator.comparingInt(PromotionRule::priority))
                .toList();

        if (chainHead != null) {
            for (PromotionRule rule : sortedRules) {
                chainHead.handle(rule, context);
            }
        }

        return builder.build();
    }

    public void applyRule(PromotionRule rule, PromotionContext context) {
        PromotionStrategy strategy = strategies.get(rule.type());
        if (strategy != null) {
            strategy.apply(rule, context);
        }
    }

    public static BigDecimal percentageOf(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static boolean isVip(CustomerType customerType) {
        return customerType == CustomerType.VIP;
    }
}
