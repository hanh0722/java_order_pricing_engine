package com.ecommerce.pricing.infrastructure.config;

import com.ecommerce.pricing.domain.port.PromotionRulePort;
import com.ecommerce.pricing.domain.promotion.PromotionPipeline;
import com.ecommerce.pricing.domain.promotion.PromotionStrategy;
import com.ecommerce.pricing.domain.promotion.chain.CouponPromotionHandler;
import com.ecommerce.pricing.domain.promotion.chain.ItemLevelPromotionHandler;
import com.ecommerce.pricing.domain.promotion.chain.OrderLevelPromotionHandler;
import com.ecommerce.pricing.domain.promotion.strategy.BuyXGetYStrategy;
import com.ecommerce.pricing.domain.promotion.strategy.CouponFixedStrategy;
import com.ecommerce.pricing.domain.promotion.strategy.PercentageOrderStrategy;
import com.ecommerce.pricing.domain.promotion.strategy.VipCustomerStrategy;
import com.ecommerce.pricing.domain.service.OrderPricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PromotionConfig {

    @Bean
    public PromotionPipeline promotionPipeline() {
        List<PromotionStrategy> strategyList = List.of(
                new BuyXGetYStrategy(),
                new PercentageOrderStrategy(),
                new VipCustomerStrategy(),
                new CouponFixedStrategy());

        PromotionPipeline pipeline = new PromotionPipeline(strategyList);

        ItemLevelPromotionHandler itemHandler = new ItemLevelPromotionHandler(pipeline);
        OrderLevelPromotionHandler orderHandler = new OrderLevelPromotionHandler(pipeline);
        CouponPromotionHandler couponHandler = new CouponPromotionHandler(pipeline);

        itemHandler.linkWith(orderHandler).linkWith(couponHandler);
        pipeline.setChainHead(itemHandler);

        return pipeline;
    }

    @Bean
    public OrderPricingService orderPricingService(
            PromotionPipeline promotionPipeline,
            PromotionRulePort promotionRulePort) {
        return new OrderPricingService(promotionPipeline, promotionRulePort);
    }
}
