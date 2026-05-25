package com.ecommerce.pricing.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CalculateOrderRequest(
        String customerType,
        @NotEmpty @Valid List<OrderItemRequest> items,
        String couponCode) {}
