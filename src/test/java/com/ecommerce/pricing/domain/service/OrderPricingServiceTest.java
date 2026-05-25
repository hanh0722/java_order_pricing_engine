package com.ecommerce.pricing.domain.service;

import com.ecommerce.pricing.domain.model.CustomerType;
import com.ecommerce.pricing.domain.model.OrderItem;
import com.ecommerce.pricing.domain.model.PricingRequest;
import com.ecommerce.pricing.domain.model.PricingResult;
import com.ecommerce.pricing.domain.port.PromotionRulePort;
import com.ecommerce.pricing.domain.promotion.PromotionPipeline;
import com.ecommerce.pricing.domain.promotion.PromotionRule;
import com.ecommerce.pricing.domain.promotion.PromotionType;
import com.ecommerce.pricing.domain.promotion.chain.CouponPromotionHandler;
import com.ecommerce.pricing.domain.promotion.chain.ItemLevelPromotionHandler;
import com.ecommerce.pricing.domain.promotion.chain.OrderLevelPromotionHandler;
import com.ecommerce.pricing.domain.promotion.strategy.BuyXGetYStrategy;
import com.ecommerce.pricing.domain.promotion.strategy.CouponFixedStrategy;
import com.ecommerce.pricing.domain.promotion.strategy.PercentageOrderStrategy;
import com.ecommerce.pricing.domain.promotion.strategy.VipCustomerStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPricingServiceTest {

    @Mock
    private PromotionRulePort promotionRulePort;

    private OrderPricingService orderPricingService;

    @BeforeEach
    void setUp() {
        List<com.ecommerce.pricing.domain.promotion.PromotionStrategy> strategies = List.of(
                new BuyXGetYStrategy(),
                new PercentageOrderStrategy(),
                new VipCustomerStrategy(),
                new CouponFixedStrategy());

        PromotionPipeline pipeline = new PromotionPipeline(strategies);
        ItemLevelPromotionHandler itemHandler = new ItemLevelPromotionHandler(pipeline);
        OrderLevelPromotionHandler orderHandler = new OrderLevelPromotionHandler(pipeline);
        CouponPromotionHandler couponHandler = new CouponPromotionHandler(pipeline);
        itemHandler.linkWith(orderHandler).linkWith(couponHandler);
        pipeline.setChainHead(itemHandler);

        orderPricingService = new OrderPricingService(pipeline, promotionRulePort);
    }

    @Test
    void calculatesChallengeExample() {
        when(promotionRulePort.findActivePromotions()).thenReturn(List.of(
                new PromotionRule("ORDER10PCT", PromotionType.PERCENTAGE_ORDER,
                        new BigDecimal("10"), null, null, null, 200)));
        when(promotionRulePort.findCouponRule("SUMMER10")).thenReturn(Optional.of(
                new PromotionRule("SUMMER10", PromotionType.COUPON_FIXED,
                        new BigDecimal("10"), null, null, null, 400)));

        PricingRequest request = new PricingRequest(
                CustomerType.VIP,
                List.of(
                        new OrderItem("A100", new BigDecimal("100"), 2),
                        new OrderItem("B200", new BigDecimal("50"), 1)),
                "SUMMER10");

        PricingResult result = orderPricingService.calculatePrice(request);

        assertEquals(new BigDecimal("250.00"), result.subtotal());
        assertEquals(new BigDecimal("35.00"), result.discount());
        assertEquals(new BigDecimal("215.00"), result.finalPrice());
    }
}
