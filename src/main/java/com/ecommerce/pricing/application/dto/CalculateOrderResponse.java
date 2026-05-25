package com.ecommerce.pricing.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CalculateOrderResponse(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal finalPrice,
        Long orderId,
        List<AppliedPromotionResponse> appliedPromotions) {

    public record AppliedPromotionResponse(String code, BigDecimal amount) {}
}
