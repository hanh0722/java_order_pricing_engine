package com.ecommerce.pricing.application;

import com.ecommerce.pricing.application.dto.CalculateOrderRequest;
import com.ecommerce.pricing.application.dto.CalculateOrderResponse;
import com.ecommerce.pricing.domain.model.CustomerType;
import com.ecommerce.pricing.domain.model.OrderItem;
import com.ecommerce.pricing.domain.model.PricingRequest;
import com.ecommerce.pricing.domain.model.PricingResult;
import com.ecommerce.pricing.domain.port.CouponUsagePort;
import com.ecommerce.pricing.domain.port.OrderPersistencePort;
import com.ecommerce.pricing.domain.service.OrderPricingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderPricingApplicationService {

    private final OrderPricingService orderPricingService;
    private final OrderPersistencePort orderPersistencePort;
    private final CouponUsagePort couponUsagePort;

    public OrderPricingApplicationService(
            OrderPricingService orderPricingService,
            OrderPersistencePort orderPersistencePort,
            CouponUsagePort couponUsagePort) {
        this.orderPricingService = orderPricingService;
        this.orderPersistencePort = orderPersistencePort;
        this.couponUsagePort = couponUsagePort;
    }

    @Transactional
    public CalculateOrderResponse calculate(CalculateOrderRequest request) {
        PricingRequest pricingRequest = toDomain(request);

        // Reserve coupon under row lock before pricing (prevents over-subscription under concurrency)
        if (hasCoupon(pricingRequest)) {
            couponUsagePort.reserve(pricingRequest.couponCode());
        }

        PricingResult result = orderPricingService.calculatePrice(pricingRequest);
        Long orderId = orderPersistencePort.saveCalculatedOrder(pricingRequest, result);
        return toResponse(result, orderId);
    }

    private boolean hasCoupon(PricingRequest request) {
        return request.couponCode() != null && !request.couponCode().isBlank();
    }

    private PricingRequest toDomain(CalculateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(item.sku(), item.price(), item.quantity()))
                .toList();

        return new PricingRequest(
                CustomerType.from(request.customerType()),
                items,
                request.couponCode());
    }

    private CalculateOrderResponse toResponse(PricingResult result, Long orderId) {
        List<CalculateOrderResponse.AppliedPromotionResponse> applied = result.appliedPromotions().stream()
                .map(p -> new CalculateOrderResponse.AppliedPromotionResponse(p.code(), p.amount()))
                .toList();

        return new CalculateOrderResponse(
                result.subtotal(),
                result.discount(),
                result.finalPrice(),
                orderId,
                applied);
    }
}
